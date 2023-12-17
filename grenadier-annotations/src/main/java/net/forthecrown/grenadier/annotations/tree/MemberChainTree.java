package net.forthecrown.grenadier.annotations.tree;

import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.annotations.util.Utils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/**
 * Reference to a method/field inside a class
 *
 * @param name Name of the member
 * @param kind Member's type
 * @param next Next reference in the chain of references,
 *             {@code null}, if there is no next node
 */
@Internal
public record MemberChainTree(
    String name,
    Kind kind,
    MemberChainTree next
) {

  public MemberChainTree(String name, Kind kind, MemberChainTree next) {
    this.name = Objects.requireNonNull(name);
    this.kind = Objects.requireNonNull(kind);
    this.next = next;
  }

  public @NotNull Pair<Object, MemberChainTree> resolveLast(Object o) {
    if (next == null) {
      return Pair.of(o, this);
    }

    Class<?> clazz = o.getClass();

    try {
      Object value = kind.resolve(clazz, o, name);
      return next.resolveLast(value);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return null;
    }
  }

  public Result<Field> getField(Class<?> declaring) {
    assert kind == Kind.FIELD;

    try {
      return Result.success(declaring.getDeclaredField(name));
    } catch (NoSuchFieldException e) {
      return Result.fail("No such method '%s' in class %s", name, declaring);
    }
  }

  public Result<Method> getMethod(Class<?> declaring,
                                  Class<?>... paramTypes
  ) {
    assert kind == Kind.METHOD;

    try {
      return Result.success(declaring.getDeclaredMethod(name, paramTypes));
    } catch (NoSuchMethodException e) {
      return Result.fail("No such method '%s' in %s with params %s",
          name, declaring,
          Arrays.toString(paramTypes)
      );
    }
  }

  public Stream<Method> findMatching(Class<?> declaring,
                                     Predicate<Method> methodPredicate
  ) {
    List<Method> matching = new ArrayList<>();
    addMethods(declaring, matching, name, methodPredicate);
    return matching.stream();
  }

  public Result<Method> findUniqueMethod(Class<?> declaring) {
    List<Method> matching = new ArrayList<>();
    addMethods(declaring, matching, name, null);

    if (matching.isEmpty()) {
      return Result.fail("No method named '%s' found in %s", name, declaring);
    }

    if (matching.size() > 1) {
      return Result.fail(
          "Found too many methods named '%s' in %s. "
              + "Cannot resolve a single one",
          name, declaring
      );
    }

    return Result.success(matching.iterator().next());
  }

  private static void addMethods(Class<?> c,
                                 List<Method> methods,
                                 String name,
                                 Predicate<Method> predicate
  ) {
    var stream = Arrays.stream(c.getDeclaredMethods())
        .filter(method -> method.getName().equals(name));

    if (predicate != null) {
      stream = stream.filter(predicate);
    }

    stream.forEach(methods::add);

    Class<?>   superClass = c.getSuperclass();
    Class<?>[] interfaces = c.getInterfaces();

    if (superClass != null) {
      addMethods(superClass, methods, name, predicate);
    }

    if (interfaces.length > 0) {
      for (Class<?> anInterface : interfaces) {
        addMethods(anInterface, methods, name, predicate);
      }
    }
  }

  public String path() {
    StringBuilder builder = new StringBuilder();
    appendTo(builder);
    return builder.toString();
  }

  public void appendTo(StringBuilder builder) {
    builder.append(name);

    if (kind == Kind.METHOD) {
      builder.append("()");
    }

    if (next != null) {
      builder.append(".");
      next.appendTo(builder);
    }
  }

  public Object resolve(Object o) {
    try {
      return kind.resolve(o.getClass(), o, name);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return null;
    }
  }

  public enum Kind {
    METHOD {
      @Override
      public Object resolve(Class<?> clazz, Object o, String name)
          throws ReflectiveOperationException
      {
        var m = clazz.getDeclaredMethod(name);
        return m.invoke(o);
      }
    },

    FIELD {
      @Override
      public Object resolve(Class<?> clazz, Object o, String name)
          throws ReflectiveOperationException
      {
        var f = clazz.getDeclaredField(name);
        return f.get(o);
      }
    };

    public abstract Object resolve(Class<?> clazz, Object o, String name)
        throws ReflectiveOperationException;
  }
}
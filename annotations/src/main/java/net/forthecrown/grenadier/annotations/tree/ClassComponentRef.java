package net.forthecrown.grenadier.annotations.tree;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.util.Objects;
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
public record ClassComponentRef(
    String name,
    Kind kind,
    ClassComponentRef next
) {

  public ClassComponentRef(String name, Kind kind, ClassComponentRef next) {
    this.name = Objects.requireNonNull(name);
    this.kind = Objects.requireNonNull(kind);
    this.next = next;
  }

  public @NotNull Pair<Object, ClassComponentRef> resolveLast(Object o) {
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

  public @NotNull Method findMethod(Object o, Class<?>[] params) {
    Preconditions.checkState(this.kind == Kind.METHOD, "Not a method");

    Class<?> clazz = o.getClass();

    try {
      return clazz.getDeclaredMethod(name, params);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return null;
    }
  }

  public <R, I> @NotNull R execute(
      Class<R> resultType,
      Class<I> providerType,
      ThrowingException<I, R> providerFunction,
      Class<?>[] params,
      Object commandClass,
      Object... paramObjects
  ) throws CommandSyntaxException {
    Pair<Object, ClassComponentRef> last = resolveLast(commandClass);

    var o = last.left();
    var lastRef = last.right();

    if (lastRef.kind() == Kind.METHOD) {
      Method m = lastRef.findMethod(o, params);

      Preconditions.checkState(
          m.getReturnType() == resultType,
          "Test method '%s' must return a %s",
          lastRef.name(),
          resultType.getSimpleName()
      );

      try {
        Object value = m.invoke(o, paramObjects);
        return (R) value;
      } catch (ReflectiveOperationException exc) {
        Utils.sneakyThrow(exc);
        return null;
      }
    }

    Object predicateObject = lastRef.resolve(o);

    Preconditions.checkState(
        providerType.isInstance(predicateObject),
        "Field '%s' must be a %s", providerType
    );

    I predicate = (I) predicateObject;
    return providerFunction.apply(predicate);
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

  public interface ThrowingException<I, R> {
    R apply(I i) throws CommandSyntaxException;
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
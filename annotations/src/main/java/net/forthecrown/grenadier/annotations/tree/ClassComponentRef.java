package net.forthecrown.grenadier.annotations.tree;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.grenadier.CommandContexts;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.Utils;
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

  public int runAsExecutes(Object object,
                           CommandContext<CommandSource> context
  ) {
    Preconditions.checkState(next == null, "next() node present");

    Map<String, ParsedArgument<CommandSource, ?>> arguments
        = CommandContexts.getArguments(context);

    Class<?> objectClass = object.getClass();

    var methods = Arrays.stream(objectClass.getMethods())
        .filter(method -> method.getName().equals(this.name))
        .toList();

    Preconditions.checkState(
        !methods.isEmpty(),
        "No executes method named '%s' found in %s",
        name, object
    );

    Preconditions.checkState(
        methods.size() < 2,
        "Found more than 1 method named '%s' in %s",
        name, object
    );

    Method m = methods.iterator().next();
    Parameter[] params = m.getParameters();

    int contextIndex = -1;
    Object[] invocationParameters = new Object[params.length];

    for (int i = 0; i < params.length; i++) {
      var p = params[i];

      if (p.getType() == CommandContext.class) {
        Preconditions.checkState(
            contextIndex == -1,
            "Cannot have more than 1 CommandContext parameter"
        );

        contextIndex = i;
        invocationParameters[i] = context;

        continue;
      }

      String argumentName;
      boolean optional;

      if (p.isAnnotationPresent(Argument.class)) {
        var arg = p.getAnnotation(Argument.class);
        argumentName = arg.value();
        optional = arg.optional();
      } else {
        argumentName = p.getName();
        optional = false;
      }

      ParsedArgument<CommandSource, ?> argument = arguments.get(argumentName);

      if (argument == null) {
        if (optional) {
          invocationParameters[i] = null;
          continue;
        }

        throw new NullPointerException(String.format(
            "No argument named '%s' in command",
            argumentName
        ));
      }

      Class<?> parameterType = CommandContexts.PRIMITIVE_TO_WRAPPER
          .getOrDefault(p.getType(), p.getType());

      Preconditions.checkState(
          parameterType.isAssignableFrom(argument.getResult().getClass()),
          "Argument '%s' is defined as %s not %s",
          argumentName,
          argument.getResult().getClass(),
          parameterType
      );

      invocationParameters[i] = argument.getResult();
    }

    Object result;

    try {
      result = m.invoke(object, invocationParameters);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return 1;
    }

    if (result instanceof Integer integer) {
      return integer;
    }

    return 0;
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
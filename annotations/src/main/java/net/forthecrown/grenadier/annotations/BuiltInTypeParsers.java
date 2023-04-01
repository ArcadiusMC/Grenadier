package net.forthecrown.grenadier.annotations;

import static net.forthecrown.grenadier.annotations.TokenType.IDENTIFIER;
import static net.forthecrown.grenadier.annotations.TokenType.QUOTED_STRING;
import static net.forthecrown.grenadier.annotations.TokenType.VARIABLE;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import jdk.jfr.Event;
import net.forthecrown.grenadier.annotations.TypeRegistry.TypeParser;
import net.forthecrown.grenadier.annotations.compiler.CompileContext;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.TypeInfoTree;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.MapArgument;

final class BuiltInTypeParsers {
  private BuiltInTypeParsers() {}

  @SuppressWarnings("unchecked")
  static final TypeParser<ArrayArgument<?>> ARRAY = (info, ctx) -> {
    return info.getOption("values")
        .flatMap(token -> token.parseExpect(VARIABLE))
        .flatMap(token -> ctx.getVariable(token.value(), ArgumentType.class))
        .map(ArgumentTypes::array);
  };

  @SuppressWarnings("unchecked")
  static final TypeParser<MapArgument<?>> MAP = (info, ctx) -> {
    return info.getOption("values")
        .flatMap(token -> token.parseExpect(VARIABLE))
        .flatMap(token -> ctx.getVariable(token.value(), Map.class))
        .map(map -> ArgumentTypes.map(map));
  };

  static final TypeParser<EnumArgument<?>> ENUM = (info, context) -> {
    return info.getOption("type")
        .flatMap(token -> {
          return token.parseExpect(IDENTIFIER, QUOTED_STRING, VARIABLE);
        })

        .flatMap(token -> parseEnumToken(token, context, info));
  };

  @SuppressWarnings("unchecked")
  private static Result<EnumArgument<?>> parseEnumToken(
      Token token,
      CompileContext context,
      TypeInfoTree info
  ) {
    Result<Class<?>> enumType;

    if (!token.is(IDENTIFIER, QUOTED_STRING)) {
      Object value = context.getVariables().get(token.value());

      if (value == null) {
        return Result.fail(token.position(),
            "Option '%s' is undefined for argument type '%s'",
            token.value(), info.name()
        );
      }

      if (value instanceof Class<?> enumClass) {
        enumType = Result.success(enumClass);
      } else if (value instanceof String string) {
        enumType = readClassName(string, context.getLoader());
      } else {
        return Result.fail(token.position(),

            "Variable '%s' must be either an enum class or enum class "
                + "name for argument type '%s'",

            token.value(),
            info.name()
        );
      }
    } else {
      enumType = readClassName(token.value(), context.getLoader());
    }

    return enumType.flatMap(aClass -> {
      if (Event.class.isAssignableFrom(aClass)) {
        return Result.success(aClass);
      }

      return Result.fail(token.position(),
          "Class '%s' is not an enum class", aClass.getName()
      );
    }).map(aClass -> {
      Class<? extends Enum> enumClass = (Class<? extends Enum>) aClass;
      return ArgumentTypes.enumType(enumClass);
    });
  }

  private static Result<Class<?>> readClassName(
      String className,
      ClassLoader loader
  ) {
    try {
      return Result.success(Class.forName(className, true, loader));
    } catch (ClassNotFoundException exc) {
      return Result.fail("Unknown enum class '%s', not found", className);
    }
  }

  static final TypeParser<IntegerArgumentType> INT = numberType(
      Integer.MIN_VALUE,
      Integer.MAX_VALUE,
      Integer::parseInt,
      IntegerArgumentType::integer
  );

  static final TypeParser<DoubleArgumentType> DOUBLE = numberType(
      Double.MIN_VALUE,
      Double.MAX_VALUE,
      Double::parseDouble,
      DoubleArgumentType::doubleArg
  );

  static final TypeParser<FloatArgumentType> FLOAT = numberType(
      Float.MIN_VALUE,
      Float.MAX_VALUE,
      Float::parseFloat,
      FloatArgumentType::floatArg
  );

  static final TypeParser<LongArgumentType> LONG = numberType(
      Long.MIN_VALUE,
      Long.MAX_VALUE,
      Long::parseLong,
      LongArgumentType::longArg
  );

  @SuppressWarnings({"rawtypes", "unchecked"})
  static <N extends Number, T extends ArgumentType<N>> TypeParser<T> numberType(
      N minValue,
      N maxValue,
      Function<String, N> parser,
      BiFunction<N, N, T> factory
  ) {
    return (info, context) -> {
      N min = minValue;
      N max = maxValue;

      Token minToken = info.options().get("min");
      Token maxToken = info.options().get("max");

      if (minToken != null) {
        Result res = minToken.parseExpect(IDENTIFIER);

        if (res.isError()) {
          return res;
        }

        min = parser.apply(minToken.value());
      }

      if (maxToken != null) {
        Result res = maxToken.parseExpect(IDENTIFIER);

        if (res.isError()) {
          return res;
        }

        max = parser.apply(maxToken.value());
      }

      return Result.success(factory.apply(min, max));
    };
  }
}
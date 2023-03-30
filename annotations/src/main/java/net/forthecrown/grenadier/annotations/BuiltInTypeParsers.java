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
import net.forthecrown.grenadier.annotations.TypeRegistry.TypeParser;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.MapArgument;

final class BuiltInTypeParsers {
  private BuiltInTypeParsers() {}

  static final TypeParser<ArrayArgument<?>> ARRAY = (info, context, factory) -> {
    var token = info.getOrThrow("values", factory);
    token.expect(factory, VARIABLE);

    String label = token.value();

    ArgumentType<?> type = context.getOrThrow(label, ArgumentType.class);
    return ArgumentTypes.array(type);
  };

  static final TypeParser<MapArgument<?>> MAP = (info, context, factory) -> {
    var token = info.getOrThrow("values", factory);
    token.expect(factory, VARIABLE);

    String label = token.value();

    Map<String, ?> values = context.getOrThrow(label, Map.class);
    return ArgumentTypes.map(values);
  };

  static final TypeParser<EnumArgument<?>> ENUM = (info, context, factory) -> {
    var token = info.getOrThrow("type", factory);
    token.expect(factory, IDENTIFIER, QUOTED_STRING, VARIABLE);

    Class<?> enumType;

    if (!token.is(IDENTIFIER, QUOTED_STRING)) {
      Object value = context.variables().get(token.value());

      if (value == null) {
        throw factory.create(
            "Option '%s' is undefined for argument type '%s'",
            token.value(), info.name()
        );
      }

      if (value instanceof Class<?> enumClass) {
        enumType = enumClass;
      } else if (value instanceof String string) {
        enumType = readClassName(string, context.loader(), factory);
      } else {
        throw factory.create(
            "Variable '%s' must be either an enum class or enum class "
                + "name for argument type '%s'",

            token.value(),
            info.name()
        );
      }
    } else {
      enumType = readClassName(token.value(), context.loader(), factory);
    }

    if (!Enum.class.isAssignableFrom(enumType)) {
      throw factory.create(
          "Class '%s' is not an enum class", enumType.getName()
      );
    }

    @SuppressWarnings("unchecked") // We literally checked it above
    Class<? extends Enum> enumClass = (Class<? extends Enum>) enumType;

    return ArgumentTypes.enumType(enumClass);
  };

  private static Class<?> readClassName(String className,
                                        ClassLoader loader,
                                        ParseExceptions exceptions
  ) {
    try {
      return Class.forName(className, true, loader);
    } catch (ClassNotFoundException exc) {
      throw exceptions.create(
          "Unknown enum class '%s', not found", className
      );
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

  static <N extends Number, T extends ArgumentType<N>> TypeParser<T> numberType(
      N minValue,
      N maxValue,
      Function<String, N> parser,
      BiFunction<N, N, T> factory
  ) {
    return (info, context, exceptions) -> {
      N min = minValue;
      N max = maxValue;

      Token minToken = info.options().get("min");
      Token maxToken = info.options().get("max");

      if (minToken != null) {
        minToken.expect(exceptions, IDENTIFIER);
        min = parser.apply(minToken.value());
      }

      if (maxToken != null) {
        maxToken.expect(exceptions, IDENTIFIER);
        max = parser.apply(maxToken.value());
      }

      return factory.apply(min, max);
    };
  }
}
package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.compiler.ParamFiller.ArgumentFiller;
import net.forthecrown.grenadier.annotations.util.ExpandedCommandContext;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.annotations.util.Utils;
import org.jetbrains.annotations.Nullable;

class ParamFillers {

  static final int UNSET = -1;

  public static Result<ParamFiller[]> compile(
      Method method,
      CompileContext context,
      boolean suggestionsAllowed,
      final int pos
  ) {
    Parameter[] params = method.getParameters();
    ParamFiller[] result = new ParamFiller[params.length];

    if (params.length == 0) {
      context.getErrors().warning(pos,
          "No CommandSource or CommandContext%s parameters set",
          suggestionsAllowed ? " or SuggestionsBuilder" : ""
      );

      return Result.success(result);
    }

    int contextIndex = UNSET;
    int sourceIndex  = UNSET;
    int builderIndex = UNSET;
    boolean failed   = false;

    for (int i = 0; i < params.length; i++) {
      Parameter param = params[i];
      Class<?> type = param.getType();

      if (type == CommandContext.class) {
        if (contextIndex != UNSET) {
          context.getErrors().error(pos, "CommandContext parameter set twice");
          failed = true;

          continue;
        }

        contextIndex = i;
        result[i] = ParamFiller.CONTEXT;

        continue;
      }

      if (type == CommandSource.class) {
        if (sourceIndex != UNSET) {
          context.getErrors().error(pos, "CommandSource parameter set twice");
          failed = true;
          continue;
        }

        sourceIndex = i;
        result[i] = ParamFiller.SOURCE;

        continue;
      }

      if (type == SuggestionsBuilder.class && suggestionsAllowed) {
        if (builderIndex != UNSET) {
          context.getErrors().error(pos,
              "SuggestionsBuilder parameter set twice"
          );

          failed = true;
          continue;
        }

        builderIndex = i;
        result[i] = ParamFiller.BUILDER;

        continue;
      }

      String argumentName;
      boolean optional;

      if (param.isAnnotationPresent(Argument.class)) {
        Argument argument = param.getAnnotation(Argument.class);
        argumentName = argument.value();
        optional = argument.optional();
      } else {
        if (!param.isNamePresent()) {
          context.getErrors().warning(pos,
              "Parameter %s in method '%s' is missing the original parameter "
                  + "name, this likely means the parameter name doesn't line "
                  + "up with the parameter name",

              param, method
          );
        }

        argumentName = param.getName();
        optional = false;
      }

      var available = context.getAvailableArguments();
      if (!available.contains(argumentName) && !optional) {
        context.getErrors().error(pos,
            "Argument '%s' is not available in the current context",
            argumentName
        );

        failed = true;
        continue;
      }

      if (param.getType().isPrimitive() && optional) {
        context.getErrors().error(pos,
            "Argument '%s' (param: '%s') is marked as optional despite being a "
                + "primitive, will cause issues if this argument is missing",

            argumentName,
            param.getName()
        );

        failed = true;
        continue;
      }

      ParamFiller filler = argument(
          argumentName,
          optional,
          Utils.primitiveToWrapper(param.getType())
      );

      result[i] = filler;
    }

    if (failed) {
      return Result.fail(pos, "Failed to compile method's parameters");
    }

    if (sourceIndex == UNSET && contextIndex == UNSET) {
      context.getErrors().warning(pos,
          "No CommandSource or CommandContext parameters set"
      );
    }

    if (suggestionsAllowed && builderIndex == UNSET) {
      context.getErrors().warning(pos, "No SuggestionsBuilder parameter set");
    }

    return Result.success(result);
  }

  public static ParamFiller argument(String name,
                                     boolean optional,
                                     Class<?> type
  ) {
    return new ArgumentFiller(name, optional, type);
  }

  public static void fill(ParamFiller[] fillers,
                          Object[] parameterBuffer,
                          ExpandedCommandContext context,
                          @Nullable SuggestionsBuilder builder
  ) {
    for (int i = 0; i < fillers.length; i++) {
      parameterBuffer[i] = fillers[i].getValue(context, builder);
    }
  }
}
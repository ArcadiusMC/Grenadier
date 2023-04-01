package net.forthecrown.grenadier.annotations.compiler;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandContexts;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.util.Utils;
import net.forthecrown.grenadier.annotations.tree.ArgumentValue;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef.Kind;

@RequiredArgsConstructor
class CompiledExecutes implements Command<CommandSource> {

  private final ClassComponentRef ref;
  private final Object commandClass;

  final Map<String, List<ArgumentModifier>> modifierMap;

  @Override
  public int run(CommandContext<CommandSource> context)
      throws CommandSyntaxException
  {
    Pair<Object, ClassComponentRef> last = ref.resolveLast(commandClass);

    ClassComponentRef lastRef = last.right();
    Object handle = last.left();

    if (lastRef.kind() == Kind.FIELD) {
      Object value = lastRef.resolve(handle);

      Preconditions.checkState(
          value instanceof Command<?>,
          "'%s' does not point to a Command<CommandSource> interface",
          ref.path()
      );

      return ((Command<CommandSource>) value)
          .run(context);
    }

    Map<String, ParsedArgument<CommandSource, ?>> arguments
        = CommandContexts.getArguments(context);

    // Apply any existing argument modifiers
    Map<String, ArgumentValue<?>> argumentValues = new HashMap<>();
    arguments.forEach((s, arg) -> {
      ArgumentValue value = new ArgumentValue(arg.getResult());

      List<ArgumentModifier> modifierList = modifierMap.get(s);

      if (modifierList != null && modifierList.size() > 0) {
        List<Object> mappedValues = new ArrayList<>();
        value.setMappedValues(mappedValues);

        modifierList.forEach(modifier -> {
          ArgumentModifier unchecked = modifier;
          Object val = unchecked.apply(context, arg.getResult());
          mappedValues.add(val);
        });
      }

      argumentValues.put(s, value);
    });

    Class<?> objectClass = handle.getClass();

    var methods = Arrays.stream(objectClass.getMethods())
        .filter(method -> method.getName().equals(lastRef.name()))
        .toList();

    Preconditions.checkState(
        !methods.isEmpty(),
        "No executes method named '%s' found in %s",
        lastRef.name(), handle
    );

    Preconditions.checkState(
        methods.size() < 2,
        "Found more than 1 method named '%s' in %s",
        lastRef.name(), handle
    );

    Method method = methods.iterator().next();
    Parameter[] params = method.getParameters();

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

      ArgumentValue<?> argument = argumentValues.get(argumentName);

      if (argument == null) {
        if (optional) {
          invocationParameters[i] = null;
          continue;
        }

        throw new IllegalStateException(String.format(
            "No argument named '%s' in command",
            argumentName
        ));
      }

      Class<?> parameterType = CommandContexts.PRIMITIVE_TO_WRAPPER
          .getOrDefault(p.getType(), p.getType());

      Object value = argument.findValue(parameterType);

      if (value == null) {
        Preconditions.checkState(
            parameterType.isAssignableFrom(argument.getValue().getClass()),
            "Argument '%s' is defined as %s not %s",
            argumentName,
            argument.getValue().getClass(),
            parameterType
        );

        continue;
      }

      invocationParameters[i] = value;
    }

    Object result;

    try {
      result = method.invoke(handle, invocationParameters);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return 1;
    }

    if (result instanceof Integer integer) {
      return integer;
    }

    return 0;
  }
}
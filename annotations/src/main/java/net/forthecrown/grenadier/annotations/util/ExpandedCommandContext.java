package net.forthecrown.grenadier.annotations.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.grenadier.CommandContexts;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.tree.ArgumentValue;

public class ExpandedCommandContext {
  private final CommandContext<CommandSource> base;
  private Map<String, ArgumentValue<?>> values = new HashMap<>();

  public ExpandedCommandContext(CommandContext<CommandSource> base) {
    this.base = base;

    Map<String, ParsedArgument<CommandSource, ?>> argumentMap
        = CommandContexts.getArguments(base);

    argumentMap.forEach((s, argument) -> {
      values.put(s, new ArgumentValue<>(argument.getResult()));
    });
  }

  public Object getValue(String name, Class<?> type, boolean optional) {
    ArgumentValue<?> value = values.get(name);

    if (value == null && optional) {
      return null;
    }

    Objects.requireNonNull(value, "No value named " + name + " found");

    Object result = value.findValue(type);
    Objects.requireNonNull(result, "No value of type " + type + " found");

    return result;
  }

  public void applyAll(Map<String, List<ArgumentModifier<?, ?>>> mappers)
      throws CommandSyntaxException
  {
    for (var entry : mappers.entrySet()) {
      String s = entry.getKey();
      List<ArgumentModifier<?, ?>> argumentModifiers = entry.getValue();
      ArgumentValue<?> value = values.get(s);

      if (value == null) {
        continue;
      }

      for (ArgumentModifier<?, ?> modifier : argumentModifiers) {
        ArgumentModifier mod = modifier;
        Object result = mod.apply(base, value.getValue());

        if (value.getMappedValues() == null) {
          value.setMappedValues(new ArrayList<>());
        }

        value.getMappedValues().add(result);
      }
    }
  }

  public CommandContext<CommandSource> getBase() {
    return base;
  }

  public CommandSource getSource() {
    return base.getSource();
  }
}
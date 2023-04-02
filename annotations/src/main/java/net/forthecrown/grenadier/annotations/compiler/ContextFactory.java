package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.util.ExpandedCommandContext;

@RequiredArgsConstructor
public class ContextFactory {
  final Map<String, List<ArgumentModifier<?, ?>>> modifierMap;

  public ExpandedCommandContext create(CommandContext<CommandSource> context)
      throws CommandSyntaxException
  {
    ExpandedCommandContext result = new ExpandedCommandContext(context);
    result.applyAll(modifierMap);
    return result;
  }
}
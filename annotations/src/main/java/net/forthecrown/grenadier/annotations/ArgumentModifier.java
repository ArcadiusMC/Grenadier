package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.grenadier.CommandSource;

public interface ArgumentModifier<I, R> {

  R apply(CommandContext<CommandSource> context, I input);
}
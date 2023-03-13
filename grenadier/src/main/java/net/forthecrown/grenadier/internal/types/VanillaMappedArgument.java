package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;

public interface VanillaMappedArgument {
  ArgumentType<?> getVanillaType(CommandBuildContext context);

  default boolean useVanillaSuggestions() {
    return false;
  }
}
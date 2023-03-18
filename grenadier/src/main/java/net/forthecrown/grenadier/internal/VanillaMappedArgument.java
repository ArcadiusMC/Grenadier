package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface VanillaMappedArgument {
  ArgumentType<?> getVanillaType(CommandBuildContext context);

  default boolean useVanillaSuggestions() {
    return false;
  }
}
package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.Objects;
import net.forthecrown.grenadier.internal.types.VanillaMappedArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

public class TreeTranslator {
  private static boolean useVanillaSuggestions(ArgumentType<?> type) {
    if (type instanceof VanillaMappedArgument vanilla) {
      return vanilla.useVanillaSuggestions();
    }

    return false;
  }

  private static ArgumentType<?> translateType(ArgumentType<?> type) {
    if (ArgumentTypeInfos.isClassRecognized(type.getClass())) {
      return type;
    }

    if (type instanceof VanillaMappedArgument vanilla) {
      var vanillaType = vanilla.getVanillaType(InternalUtil.CONTEXT);
      Objects.requireNonNull(vanillaType, "getVanillaType returned null");

      if (!ArgumentTypeInfos.isClassRecognized(vanillaType.getClass())) {
        throw new IllegalArgumentException(
            String.format(
                "getVanillaType returned a non-vanilla argument type: %s",
                vanillaType
            )
        );
      }

      return vanillaType;
    }

    return GameProfileArgument.gameProfile();
  }

}
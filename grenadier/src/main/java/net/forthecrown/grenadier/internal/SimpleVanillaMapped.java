package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.arguments.ArgumentType;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface SimpleVanillaMapped {

  ArgumentType<?> getVanillaType();

  default boolean useVanillaSuggestions() {
    return false;
  }
}

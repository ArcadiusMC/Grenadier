package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;

public interface CommandTransformer {
  <T extends ArgumentBuilder<CommandSource, T>> void apply(T node);
}

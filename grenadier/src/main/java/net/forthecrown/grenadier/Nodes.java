package net.forthecrown.grenadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

/**
 * Utility class for creating argument builder specific to grenadier
 */
public class Nodes {

  /**
   * Creates a literal argument builder
   * @param label Argument label
   * @return Created builder
   */
  public static LiteralArgumentBuilder<CommandSource> literal(String label) {
    return LiteralArgumentBuilder.literal(label);
  }

  /**
   * Creates a required argument builder
   * @param label Argument name
   * @param type Argument type
   * @return Created builder
   * @param <T> Type
   */
  public static <T> RequiredArgumentBuilder<CommandSource, T> argument(
      String label,
      ArgumentType<T> type
  ) {
    return RequiredArgumentBuilder.argument(label, type);
  }
}
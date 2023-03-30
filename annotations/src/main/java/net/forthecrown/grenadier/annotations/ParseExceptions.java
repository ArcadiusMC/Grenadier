package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;

/**
 * {@link CommandParseException} factory
 */
@FunctionalInterface
public interface ParseExceptions {

  /**
   * Parse position value for {@link #create(int, String, Object...)} to
   * indicate there is no input context required
   */
  int NO_POS = -1;

  CommandParseException create(int pos, String format, Object... args);

  default CommandParseException create(String format, Object... args) {
    return create(NO_POS, format, args);
  }

  static ParseExceptions factory(StringReader input) {
    return new ParseExceptionsImpl(input);
  }
}
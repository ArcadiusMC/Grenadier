package net.forthecrown.grenadier.annotations;

import net.forthecrown.grenadier.annotations.util.Result;

/**
 * A character or string of characters read from input
 */
public interface Token {

  /**
   * Tests if this token's type matches any of the specified types
   * @param types Valid types
   * @return {@code true}, if this token's type is contained in the specified
   *         {@code types} array, {@code false} otherwise
   */
  boolean is(TokenType... types);

  /**
   * Expects this token's type to match the specified {@code types}
   * <p>
   * Calls {@link #is(TokenType...)}, if it returns true, a result containing
   * this token is returned, otherwise a result with a failure message is
   * returned
   *
   * @param types Valid types
   * @return Result containing this token, if this token's type matches the
   *         specified {@code types}, otherwise a failure result
   */
  Result<Token> expect(TokenType... types);

  /**
   * Gets the string value of this token. May be null, if this token's type is
   * a keyword or a single character type.
   *
   * @return Token's string value, null if the token's type is a keyword or a
   *         single character type
   */
  String value();

  /**
   * Gets the token's type
   * @return Token type
   */
  TokenType type();

  /**
   * Gets the index of the token's first character within the input it was
   * read from
   *
   * @return Token position
   */
  int position();
}
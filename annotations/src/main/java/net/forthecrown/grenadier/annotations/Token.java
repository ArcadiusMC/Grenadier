package net.forthecrown.grenadier.annotations;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.util.Arrays;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A token parse from {@link CommandData} input
 *
 * @param value Token's string value, may be empty
 * @param type Token's type
 * @param position Start of the token within the input
 */
@Internal
public record Token(String value, TokenType type, int position) {

  /**
   * Tests if this token matches any of the specified {@code types}
   *
   * @param types Types to match against
   * @return {@code true}, if this token's type matches any of the specified
   * token types, {@code false} otherwise
   */
  public boolean is(TokenType... types) {
    return Arrays.stream(types).anyMatch(tokenType -> type() == tokenType);
  }

  /**
   * Tests if this token matches any of the specified token {@code types}, if it
   * doesn't, uses the specified {@code exceptions} factory to throw a parse
   * exception
   *
   * @param exceptions Exception factory
   * @param types      Types to match against
   * @throws CommandParseException If this token's type didn't match any of the
   *                               specified types
   */
  public void expect(ParseExceptions exceptions, TokenType... types)
      throws CommandParseException
  {
    if (is(types)) {
      return;
    }

    throw exceptions.create(position,
        "Expected %s, found %s",
        typesToString(types),
        type().toString()
    );
  }

  private static String typesToString(TokenType[] types) {
    if (types.length == 1) {
      return types[0].toString();
    }

    return "one of: " + Joiner.on(", ").join(types);
  }

  @Override
  public String toString() {
    if (Strings.isNullOrEmpty(value)) {
      return type.toString();
    }

    return value;
  }
}
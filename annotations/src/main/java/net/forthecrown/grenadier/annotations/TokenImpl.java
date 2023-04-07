package net.forthecrown.grenadier.annotations;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.util.Arrays;
import net.forthecrown.grenadier.annotations.util.Result;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A token parse from {@link CommandData} input
 *
 * @param value Token's string value, may be empty
 * @param type Token's type
 * @param position Start of the token within the input
 */
@Internal
record TokenImpl(String value, TokenType type, int position) implements Token {

  @Override
  public boolean is(TokenType... types) {
    return Arrays.stream(types).anyMatch(tokenType -> type() == tokenType);
  }

  @Override
  public Result<Token> expect(TokenType... types) {
    if (is(types)) {
      return Result.success(this);
    }

    return Result.fail(position,
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
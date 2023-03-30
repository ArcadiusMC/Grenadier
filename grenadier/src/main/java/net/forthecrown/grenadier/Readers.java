package net.forthecrown.grenadier;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.regex.Pattern;

/**
 * Utility class for {@link StringReader} related things
 */
public class Readers {
  private Readers() {}

  public static final Pattern WORD_PATTERN
      = Pattern.compile("[a-zA-Z0-9_.+-]+");

  /** An empty input string reader */
  public static final StringReader EMPTY = new StringReader("");

  /**
   * Tests if the given reader starts with a specified string
   * @param reader Input to test
   * @param s String the input should start with
   * @return {@code true} if the reader starts with {@code s},
   *         {@code false} otherwise
   */
  public static boolean startsWith(StringReader reader, String s) {
    if (!reader.canRead(s.length())) {
      return false;
    }

    return reader.getRemaining().startsWith(s);
  }

  /**
   * Same as {@link #startsWith(StringReader, String)} except case is ignored
   *
   * @param reader Input to test
   * @param s String the input should start with
   * @return {@code true} if the reader starts with {@code s},
   *         {@code false} otherwise
   */
  public static boolean startsWithIgnoreCase(StringReader reader, String s) {
    if (!reader.canRead(s.length())) {
      return false;
    }

    return reader.getRemaining()
        .regionMatches(true, 0, s, 0, s.length());
  }

  /**
   * Similar to {@link #startsWithIgnoreCase(StringReader, String)}, except that
   * this method requires the specified {@code literal} be followed either by
   * the end of the input, or a whitespace character
   *
   * @param reader Reader to test
   * @param literal Literal the reader must start with
   * @return {@code true}, if the specified {@code reader} starts with the
   *         specified {@code literal} and is then followed by a whitespace or
   *         end-of-input
   */
  public static boolean startsWithArgument(StringReader reader, String literal) {
    if (!startsWithIgnoreCase(reader, literal)) {
      return false;
    }

    StringReader copied = copy(reader);
    copied.setCursor(copied.getCursor() + literal.length());

    return !copied.canRead() || Character.isWhitespace(copied.peek());
  }

  /**
   * Creates a new {@link StringReader} with the specified input and offset
   * @param input Input
   * @param cursor Offset
   * @return Created reader
   */
  public static StringReader create(String input, int cursor) {
    StringReader reader = new StringReader(input);
    reader.setCursor(cursor);
    return reader;
  }

  /**
   * Copies the input from another reader and sets the cursor offset.
   *
   * @param reader Reader to copy the input of
   * @param cursor New cursor position
   * @return Copied and offset reader
   */
  public static StringReader copy(StringReader reader, int cursor) {
    return create(reader.getString(), cursor);
  }

  /**
   * Creates a copy of the specified reader
   * @param reader Reader to copy
   * @return Copied reader
   */
  public static StringReader copy(StringReader reader) {
    return copy(reader, reader.getCursor());
  }

  /**
   * Reads until the next whitespace character or until the end of the input is
   * reached
   *
   * @param reader Reader to read from
   * @return Read string
   */
  public static String readUntilWhitespace(StringReader reader) {
    StringBuilder builder = new StringBuilder();

    while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
      builder.append(reader.read());
    }

    return builder.toString();
  }

  /**
   * Creates a reader with the specified builder's input and
   * {@link SuggestionsBuilder#getStart()} as the reader's cursor
   *
   * @param builder Input and cursor source
   * @return Created reader
   */
  public static StringReader forSuggestions(SuggestionsBuilder builder) {
    return create(builder.getInput(), builder.getStart());
  }

  /**
   * Reads a positive integer from the input.
   * <p>
   * This method ignores all non-whole number characters and only reads numeric
   * characters, the moment any other type of character, even a decimal point,
   * is reached, reading stops.
   *
   * @param reader Input
   * @param min Minimum allowed value
   * @param max Maximum allowed value
   *
   * @return Parsed integer
   *
   * @throws CommandSyntaxException If the input did not start with an integer,
   *                                was below {@code min} or above {@code max}
   */
  public static int readPositiveInt(StringReader reader, int min, int max)
      throws CommandSyntaxException
  {
    final int start = reader.getCursor();

    while (reader.canRead() && isIntegerDigit(reader.peek())) {
      reader.skip();
    }

    int end = reader.getCursor();
    String str = reader.getString().substring(start, end);

    if (str.isEmpty()) {
      reader.setCursor(start);

      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerExpectedInt()
          .createWithContext(reader);
    }

    int i = Integer.parseInt(str);

    if (i < min) {
      reader.setCursor(start);

      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .integerTooLow()
          .createWithContext(reader, i, min);
    }

    if (i > max) {
      reader.setCursor(start);

      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .integerTooHigh()
          .createWithContext(reader, i, max);
    }

    return i;
  }

  private static boolean isIntegerDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /**
   * Creates a filtered reader.
   * <p>
   * 'Filtered' means any prefixed '/' characters or any prefixed 'executes'
   * command that may be in the input is skipped, leaving us with only the input
   * that matters.
   *
   * @param input Input
   * @return Created and filtered reader
   *
   * @see #skipIrrelevantInput(StringReader) Irrelevant input skipping
   */
  public static StringReader createFiltered(String input) {
    StringReader reader = new StringReader(input);
    skipIrrelevantInput(reader);
    return reader;
  }

  /**
   * Skips any 'irrelevant' input. This means any '/' at the beginning of the
   * input is skipped. If the command is actually a 'executes' command, then all
   * irrelevant input until the 'run' argument is skipped.
   * <p>
   * Lastly, any fallback prefixes like '/grenadier:commandname' are skipped
   * as well
   *
   * @param reader Reader to skip input of
   */
  public static void skipIrrelevantInput(StringReader reader) {
    if (reader.canRead() && reader.peek() == '/') {
      reader.skip();
    }

    if (startsWith(reader, "execute")) {
      var runIndex = reader.getString().indexOf("run");

      // execute as @e facing ~ ~ ~ run /grenadier arg1 arg2
      // ^ starts with execute     ^ run index
      //                            +4 ^ corrected start index
      // That's a weird way to explain my thought process
      // but yeah

      if (runIndex != -1) {
        reader.setCursor(runIndex + 4);

        if (reader.canRead() && reader.peek() == '/') {
          reader.skip();
        }
      }
    }

    // If arguments in input:
    // /plugin:command arg1 arg2
    //                ^ Isolate from here, there
    //                  might be ':' in input
    //
    // If no arguments in input:
    // /plugin:command
    // - No space index, no need to isolate
    //
    // Isolation result:
    // /plugin:command
    //        ^ ':' index, namespace found, move cursor
    //              to compensate
    //
    // The reason the above shown isolation is done is
    // that it's not guarenteed that the input has/doesn't
    // have a namespace, so we need to test if it does,
    // not to mention, a ':' might appear in the arguments
    //

    int spaceIndex = reader.getRemaining().indexOf(' ');

    if (spaceIndex == -1) {
      spaceIndex = reader.getRemainingLength();
    }

    var subStr = reader.getRemaining().substring(0, spaceIndex);
    reader.setCursor(reader.getCursor() + subStr.indexOf(':') + 1);
  }
}
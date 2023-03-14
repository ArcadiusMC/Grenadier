package net.forthecrown.grenadier.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Utility class for {@link StringReader} related things
 */
public class Readers {
  private Readers() {}

  public static final Pattern WORD_PATTERN
      = Pattern.compile("[a-zA-Z0-9,.+-]+");

  /** An empty input string reader */
  public static final StringReader EMPTY = new StringReader("");

  public static boolean startsWith(StringReader reader, String s) {
    if (!reader.canRead(s.length())) {
      return false;
    }

    return reader.getRemaining().startsWith(s);
  }

  public static boolean startsWithIgnoreCase(StringReader reader, String s) {
    if (!reader.canRead(s.length())) {
      return false;
    }

    return reader.getRemaining()
        .regionMatches(true, 0, s, 0, s.length());
  }

  public static StringReader create(String input, int cursor) {
    StringReader reader = new StringReader(input);
    reader.setCursor(cursor);
    return reader;
  }

  public static StringReader clone(StringReader reader, int cursor) {
    return create(reader.getString(), cursor);
  }

  public static StringReader clone(StringReader reader) {
    return clone(reader, reader.getCursor());
  }

  public static String readUntilWhitespace(StringReader reader) {
    StringBuilder builder = new StringBuilder();

    while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
      builder.append(reader.read());
    }

    return builder.toString();
  }

  public static StringReader forSuggestions(SuggestionsBuilder builder) {
    return create(builder.getInput(), builder.getStart());
  }

  public static char expectOne(StringReader reader, String possibleChars)
      throws CommandSyntaxException
  {
    if (!reader.canRead() || possibleChars.indexOf(reader.peek()) == -1) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerExpectedSymbol()
          .createWithContext(reader, charArrayString(possibleChars));
    }

    return reader.read();
  }

  private static String charArrayString(String s) {
    return Arrays.toString(s.toCharArray());
  }

  public static StringReader createFiltered(String input) {
    StringReader reader = new StringReader(input);
    skipIrrelevantInput(reader);
    return reader;
  }

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
          .createWithContext(reader, i, min);
    }

    return i;
  }

  private static boolean isIntegerDigit(char c) {
    return c >= '0' && c <= '9';
  }

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
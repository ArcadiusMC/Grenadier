package net.forthecrown.grenadier.annotations.util;

import static net.forthecrown.grenadier.annotations.ParseExceptionFactory.NO_POS;

import com.mojang.brigadier.StringReader;
import java.util.Objects;
import net.forthecrown.grenadier.Readers;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ErrorMessages {
  private ErrorMessages() {}

  public static String formatError(
      StringReader input,
      int pos,
      String format,
      Object... args
  ) {
    Objects.requireNonNull(format, "Null message format");

    if (pos == NO_POS) {
      return format.formatted(args);
    }

    StringReader reader = Readers.copy(input, pos);

    final int lineStart = findLineStart(reader);
    final int lineEnd = findLineEnd(reader);

    final int pointerOffset = pos - lineStart;
    final int lineNumber = findLineNumber(reader);

    String context = reader.getString().substring(lineStart, lineEnd);

    String errorFormat = "%s\n%s\n%" + (pointerOffset + 1) + "s Line %s Column %s";

    return errorFormat.formatted(
        String.format(format, args),
        context,

        "^", lineNumber, pointerOffset
    );
  }

  private static int findLineNumber(StringReader reader) {
    int lines = 0;
    int c = reader.getCursor();

    while (c >= 0) {
      char character = reader.getString().charAt(c--);

      if (character == '\n') {
        lines++;
      }
    }

    return lines;
  }

  private static int findLineStart(StringReader reader) {
    return findLineEndStart(reader, -1);
  }

  private static int findLineEnd(StringReader reader) {
    return findLineEndStart(reader, 1);
  }

  private static int findLineEndStart(StringReader reader, int direction) {
    int r = reader.getCursor();

    while (r >= 0 && r < reader.getTotalLength()) {
      char c = reader.getString().charAt(r);

      if (c == '\n' || c == '\r') {
        int inverseDir = -direction;

        while (Character.isWhitespace(c)) {
          r += inverseDir;
          c = reader.getString().charAt(r);
        }

        return direction == -1 ? r : r + 1;
      }

      r += direction;
    }

    return Math.max(0, r);
  }
}
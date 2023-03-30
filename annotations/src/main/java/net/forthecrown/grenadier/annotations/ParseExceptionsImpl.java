package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.Readers;

@RequiredArgsConstructor
class ParseExceptionsImpl implements ParseExceptions {
  private final StringReader input;

  @Override
  public CommandParseException create(int pos, String format, Object... args) {
    Objects.requireNonNull(format, "Null message format");

    if (pos == NO_POS) {
      return new CommandParseException(format.formatted(args));
    }

    StringReader reader = Readers.copy(input, pos);

    final int lineStart = findLineStart(reader);
    final int lineEnd = findLineEnd(reader);

    final int pointerOffset = pos - lineStart;
    final int lineNumber = findLineNumber(reader);

    String context = reader.getString().substring(lineStart, lineEnd);

    String errorFormat = "%s\n%s\n%" + (pointerOffset + 1) + "s Line %s Column %s";

    return new CommandParseException(errorFormat.formatted(
        String.format(format, args),
        context,

        "^", lineNumber, pointerOffset
    ));
  }

  private int findLineNumber(StringReader reader) {
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

  private int findLineStart(StringReader reader) {
    return findLineEndStart(reader, reader.getCursor(), -1);
  }

  private int findLineEnd(StringReader reader) {
    return findLineEndStart(reader, reader.getCursor(), 1);
  }

  private int findLineEndStart(StringReader reader, int pos, int direction) {
    int r = pos;

    while (r >= 0 && r < reader.getTotalLength()) {
      char c = reader.getString().charAt(r);

      if (c == '\n' || c == '\r') {
        return direction == -1 ? r + 1 : r;
      }

      r += direction;
    }

    return Math.max(0, r);
  }
}
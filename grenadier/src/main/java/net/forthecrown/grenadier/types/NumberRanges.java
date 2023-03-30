package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.DoubleRangeArgument.DoubleRange;
import net.forthecrown.grenadier.types.DoubleRangeArgumentImpl.DoubleRangeImpl;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;
import net.forthecrown.grenadier.types.IntRangeArgumentImpl.IntRangeImpl;

final class NumberRanges {
  private NumberRanges() {}

  static final char SEPARATOR_CHAR = '.';

  public static DoubleRange parseDoubles(StringReader reader)
      throws CommandSyntaxException
  {
    return parse(
        NumberRanges::readDouble,
        DoubleRangeImpl::new,
        DoubleRangeImpl.UNLIMITED,
        reader
    );
  }

  public static IntRange parseInts(StringReader reader)
      throws CommandSyntaxException
  {
    return NumberRanges.parse(
        NumberRanges::readInt,
        IntRangeImpl::new,
        IntRangeImpl.UNLIMITED,
        reader
    );
  }

  private static <T extends NumericRange<N>, N extends Number> T parse(
      NumberParser<N> numberParser,
      RangeFactory<T, N> factory,
      T unlimited,
      StringReader reader
  ) throws CommandSyntaxException {
    if (!reader.canRead()) {
      throw Grenadier.exceptions().rangeEmpty(reader);
    }

    final int start = reader.getCursor();
    N min = numberParser.read(reader);

    if (reader.canRead(2)
        && reader.peek() == SEPARATOR_CHAR
        && reader.peek(1) == SEPARATOR_CHAR
    ) {
      reader.skip();
      reader.skip();

      N max = numberParser.read(reader);

      if (min == null && max == null) {
        return unlimited;
      }

      if (min != null && max != null && min.doubleValue() > max.doubleValue()) {
        reader.setCursor(start);
        throw Grenadier.exceptions().rangeInverted(reader);
      }

      return factory.create(min, max);
    }

    return factory.create(min, min);
  }

  private static Integer readInt(StringReader reader)
      throws CommandSyntaxException
  {
    int multiplier = readPrefix(reader);

    final int start = reader.getCursor();

    while (reader.canRead() && isAllowedInInteger(reader.peek())) {
      reader.skip();
    }

    String s = reader.getString().substring(start, reader.getCursor());

    if (s.isEmpty()) {
      return null;
    }

    try {
      int value = Integer.parseInt(s);
      return value * multiplier;
    } catch (NumberFormatException exc) {
      reader.setCursor(start);

      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerInvalidInt()
          .createWithContext(reader, s);
    }
  }

  private static boolean isAllowedInInteger(char c) {
    return c >= '0' && c <= '9';
  }

  private static Double readDouble(StringReader reader)
      throws CommandSyntaxException
  {
    double multiplier = readPrefix(reader);

    final int start = reader.getCursor();

    while (reader.canRead() && shouldContinueDoubleParse(reader)) {
      reader.skip();
    }

    String s = reader.getString().substring(start, reader.getCursor());

    if (s.isEmpty()) {
      return null;
    }

    try {
      double d = Double.parseDouble(s);
      return d * multiplier;
    } catch (NumberFormatException exc) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerInvalidDouble()
          .createWithContext(reader, s);
    }
  }

  private static boolean shouldContinueDoubleParse(StringReader reader) {
    if (isAllowedInInteger(reader.peek())) {
      return true;
    }

    if (reader.peek() != SEPARATOR_CHAR) {
      return false;
    }

    return !reader.canRead(2)
        || reader.peek() != SEPARATOR_CHAR
        || reader.peek(1) != SEPARATOR_CHAR;
  }

  private static int readPrefix(StringReader reader) {
    if (reader.canRead() && reader.peek() == '-') {
      reader.skip();
      return -1;
    }

    return 1;
  }

  public interface NumberParser<N> {
    N read(StringReader reader) throws CommandSyntaxException;
  }

  public interface RangeFactory<T extends NumericRange<N>, N> {
    T create(N min, N max);
  }

  @RequiredArgsConstructor
  static abstract class NumericRange<N> {
    protected final N min;
    protected final N max;

    public boolean isExact() {
      return min != null && max != null && Objects.equals(min, max);
    }

    @Override
    public String toString() {
      if (isExact()) {
        return min.toString();
      }

      String prefix = min == null ? "" : min.toString();
      String suffix = max == null ? "" : max.toString();

      return prefix + ".." + suffix;
    }
  }
}
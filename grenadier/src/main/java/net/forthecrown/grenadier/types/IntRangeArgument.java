package net.forthecrown.grenadier.types;

import com.google.common.collect.Range;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.OptionalInt;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;
import net.forthecrown.grenadier.types.IntRangeArgumentImpl.IntRangeImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Argument type that returns a range between 2 integer values. The range
 * parsed by this argument type is inclusive on both ends.
 *
 * <p>
 * Input examples:
 * <pre>
 * 15..35 = Between 15 and 35
 * ..17   = At most 17
 * 7..    = At least 7
 * </pre>
 */
public interface IntRangeArgument extends ArgumentType<IntRange> {

  @Override
  IntRange parse(StringReader reader) throws CommandSyntaxException;

  /**
   * Range between integer values
   */
  interface IntRange {

    /**
     * Unlimited range
     */
    IntRange UNLIMITED = IntRangeImpl.UNLIMITED;

    /**
     * Creates a new integer range
     * @param min The minimum bound, may be null
     * @param max The maximum bound, may be null
     * @return Created range
     */
    static IntRange range(@Nullable Integer min, @Nullable Integer max) {
      if (min == null && max == null) {
        return UNLIMITED;
      }
      return new IntRangeImpl(min, max);
    }

    /**
     * Creates an integer range that only matches the specified {@code value}
     * @param value Range value
     * @return Created range
     */
    static IntRange exactly(int value) {
      return range(value, value);
    }

    /**
     * Creates a new max-limited integer range
     * @param max The maximum range value
     * @return Created range
     */
    static IntRange atMost(int max) {
      return range(null, max);
    }

    /**
     * Creates a new min-limited integer range
     * @param min The minimum range value
     * @return Created range
     */
    static IntRange atLeast(int min) {
      return range(min, null);
    }

    /**
     * Gets the minimum value
     * @return Minimum value, or en empty optional, if no minimum value was set
     */
    @NotNull
    OptionalInt min();

    /**
     * Gets the maximum value
     * @return Maximum value, or an empty optional, if no maximum value was set
     */
    @NotNull
    OptionalInt max();

    /**
     * Tests if this range has a minimum limit
     * @return {@code true}, if the {@link #min()} value is present,
     *         {@code false} otherwise
     */
    default boolean isMinLimited() {
      return min().isPresent();
    }

    /**
     * Tests if this range has a maximum limit
     * @return {@code true}, if the {@link #max()} value is present,
     *         {@code false} otherwise
     */
    default boolean isMaxLimited() {
      return max().isPresent();
    }

    /**
     * Tests if this range is unlimited.
     * @return {@code true} if both {@link #min()} and {@link #max()} are empty,
     *         {@code false} otherwise
     */
    default boolean isUnlimited() {
      return max().isEmpty() && min().isEmpty();
    }

    /**
     * Tests if this range is exact
     * @return {@code true} if both {@link #min()} and {@link #max()} values are
     *         present and equal to each other, {@code false} otherwise
     */
    boolean isExact();

    /**
     * Tests if this range contains the specified {@code value}
     * @param value Value to test
     * @return {@code true} if this range contains the specified {@code value},
     *         {@code false} otherwise
     */
    boolean contains(int value);

    /**
     * Translates this parsed range into a Guava range
     * @return Range
     */
    Range<Integer> toRange();

    /**
     * Converts this range into a parse-able string
     * @return Range string
     */
    @Override
    String toString();
  }
}
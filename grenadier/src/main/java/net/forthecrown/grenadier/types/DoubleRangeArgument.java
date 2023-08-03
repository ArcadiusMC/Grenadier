package net.forthecrown.grenadier.types;

import com.google.common.collect.Range;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.OptionalDouble;
import net.forthecrown.grenadier.types.DoubleRangeArgument.DoubleRange;
import net.forthecrown.grenadier.types.DoubleRangeArgumentImpl.DoubleRangeImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Argument type that returns a range between 2 double values. The range
 * parsed by this argument type is inclusive on both ends.
 *
 * <p>
 * Input examples:
 * <pre>
 * 1..2  = Between 1 and 2
 * ..2.5 = At most 2.5
 * 3.1.. = At least 3.1
 * ..    = Unlimited range
 * </pre>
 */
public interface DoubleRangeArgument extends ArgumentType<DoubleRange> {

  @Override
  DoubleRange parse(StringReader reader) throws CommandSyntaxException;

  /**
   * Range between double values
   */
  interface DoubleRange {

    /**
     * Unlimited range
     */
    DoubleRange UNLIMITED = DoubleRangeImpl.UNLIMITED;

    /**
     * Creates a new double range
     * @param min The minimum bound, may be null
     * @param max The maximum bound, may be null
     * @return Created range
     */
    static DoubleRange range(@Nullable Double min, @Nullable Double max) {
      if (min == null && max == null) {
        return UNLIMITED;
      }
      return new DoubleRangeImpl(min, max);
    }

    /**
     * Creates a double range that only matches the specified {@code value}
     * @param value Range value
     * @return Created range
     */
    static DoubleRange exactly(double value) {
      return range(value, value);
    }

    /**
     * Creates a new max-limited double range
     * @param max The maximum range value
     * @return Created range
     */
    static DoubleRange atMost(double max) {
      return range(null, max);
    }

    /**
     * Creates a new min-limited double range
     * @param min The minimum range value
     * @return Created range
     */
    static DoubleRange atLeast(double min) {
      return range(min, null);
    }

    /**
     * Gets the minimum value
     * @return Minimum value, or en empty optional, if no minimum value was set
     */
    @NotNull
    OptionalDouble min();

    /**
     * Gets the maximum value
     * @return Maximum value, or an empty optional, if no maximum value was set
     */
    @NotNull
    OptionalDouble max();

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
    boolean contains(double value);

    /**
     * Translates this parsed range into a Guava range
     * @return Range
     */
    Range<Double> toRange();

    /**
     * Converts this range into a parse-able string
     * @return Range string
     */
    @Override
    String toString();
  }
}
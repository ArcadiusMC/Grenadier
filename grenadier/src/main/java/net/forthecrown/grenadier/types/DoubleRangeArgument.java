package net.forthecrown.grenadier.types;

import com.google.common.collect.Range;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.OptionalDouble;
import net.forthecrown.grenadier.types.DoubleRangeArgument.DoubleRange;
import net.forthecrown.grenadier.types.DoubleRangeArgumentImpl.DoubleRangeImpl;
import org.jetbrains.annotations.NotNull;

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
    default Range<Double> toRange() {
      var min = min();
      var max = max();

      if (min.isEmpty() && max.isEmpty()) {
        return Range.all();
      }

      if (max.isPresent() && min.isPresent()) {
        double minValue = min.getAsDouble();
        double maxValue = max.getAsDouble();

        return Range.closed(minValue, maxValue);
      }

      if (min.isPresent()) {
        return Range.atLeast(min.getAsDouble());
      } else {
        return Range.atMost(max.getAsDouble());
      }
    }

    /**
     * Converts this range into a parse-able string
     * @return Range string
     */
    @Override
    String toString();
  }
}
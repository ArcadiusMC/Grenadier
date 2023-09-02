package net.forthecrown.grenadier.types.options;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.ArgumentType;
import net.forthecrown.grenadier.Readers;

/**
 * Static factory class for {@link FlagOption} and {@link ArgumentOption}
 * instances and builders.
 *
 * @see #flag() Flag builder
 * @see #argument(ArgumentType) Argument builder
 */
public final class Options {
  private Options() {}

  /**
   * Creates a new flag builder
   * @return Created flag builder
   */
  public static FlagOption.Builder flag() {
    return new FlagOptionImpl.BuilderImpl();
  }

  /**
   * Creates a flag option with a single label
   * @param label Flag label
   * @return Created flag option
   */
  public static FlagOption flag(String label) {
    return flag().setLabel(label).build();
  }

  /**
   * Creates a new argument option builder
   * @param type Option type
   * @return Created builder
   * @param <T> Option value's type
   */
  public static <T> ArgumentOption.Builder<T> argument(ArgumentType<T> type) {
    return new ArgumentOptionImpl.BuilderImpl<>(type);
  }

  /**
   * Creates an argument option with a single label
   * @param type Option type
   * @param label Option label
   * @return Created option
   * @param <T> Option value's type
   */
  public static <T> ArgumentOption<T> argument(ArgumentType<T> type, String label) {
    return argument(type).setLabel(label).build();
  }

  /**
   * Tests if a specified {@code label} is a valid label for an option.
   * <p>
   * Labels have to match the {@link Readers#WORD_PATTERN} to be considered
   * valid labels.
   *
   * @param label Label to test
   * @throws IllegalArgumentException If the label was invalid
   */
  public static void validateLabel(String label)
      throws IllegalArgumentException
  {
    Preconditions.checkArgument(Readers.WORD_PATTERN.matcher(label).matches(),
        "Invalid label '%s', must match pattern %s",
        label, Readers.WORD_PATTERN
    );
  }
}
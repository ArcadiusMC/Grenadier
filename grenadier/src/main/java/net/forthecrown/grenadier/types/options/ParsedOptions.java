package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Options that were parsed by {@link OptionsArgument}
 */
public interface ParsedOptions {

  /**
   * Gets a parsed option
   * @param option Option to get the parsed representation of
   * @return Parsed option, or {@code null}, if the option was not parsed
   */
  @Nullable
  ParsedOption getParsedOption(@NotNull Option option);

  /**
   * Gets a parsed option by one of its labels
   * @param label option label
   * @return Parsed option, or {@code null} if no option with the specified
   *         {@code label} was parsed
   */
  @Nullable
  ParsedOption getParsedOption(@NotNull String label);

  /**
   * Gets a parsed value
   *
   * @param option option
   * @return parsed value
   * @param <T> value type
   */
  @Nullable
  @SuppressWarnings("unchecked") // argument options will always have ParsedValues representing them
  default <T> ParsedValue<T> getParsedValue(ArgumentOption<T> option) {
    return (ParsedValue<T>) getParsedOption(option);
  }

  /**
   * Tests if the specified {@code option} is present
   *
   * @param option Option to test for
   * @return {@code true}, if {@code option} was parsed, {@code false} otherwise
   */
  default boolean has(Option option) {
    return getParsedOption(option) != null;
  }

  /**
   * Gets an argument option's parsed value.
   * <p>
   * If the {@code option} was added as a 'required' option, then this will
   * never return null
   *
   * @param option Option
   * @return Option's value, or the {@code option}'s default value, if the
   *         option was not parsed
   * @param <T> Value type
   */
  @Nullable
  default <T> T getValue(ArgumentOption<T> option) {
    var parsedValue = getParsedValue(option);

    if (parsedValue == null) {
      return option.getDefaultValue();
    }

    return parsedValue.value();
  }

  /**
   * Gets an argument option's parsed value and tests if the specified
   * {@code source} is allowed to access its value.
   * <p>
   * If the specified {@code option} wasn't parsed, then this returns null, else
   * {@link ParsedValue#checkAccess(CommandSource)} is called with the specified
   * {@code source}, then the value is returned.
   *
   * @param option Argument option to get the value of
   * @param source Source accessing the value
   * @return Option's value, or the {@code option}'s default value, if the
   *         option wasn't parsed
   *
   * @param <T> Value type
   *
   * @throws CommandSyntaxException If the option was present and {@code source}
   *                                wasn't allowed to access it
   */
  default <T> T getValue(ArgumentOption<T> option, CommandSource source)
      throws CommandSyntaxException
  {
    var parsedValue = getParsedValue(option);

    if (parsedValue == null) {
      return option.getDefaultValue();
    }

    parsedValue.checkAccess(source);
    return parsedValue.value();
  }

  /**
   * Tests if the specified flag {@code option} is present and that the
   * specified {@code source} is allowed to access it.
   * <p>
   * If the specified flag wasn't parsed, {@code false} is returned, otherwise
   * {@link ParsedOption#checkAccess(CommandSource)} is called with the
   * specified {@code source}, then {@code true} is returned.
   *
   * @param option Flag
   * @param source Source accessing the flag
   * @return {@code true}, if the flag was parsed, {@code false} otherwise
   *
   * @throws CommandSyntaxException If the specified {@code option} was present
   *                                and the specified {@code source} wasn't
   *                                allowed to access it
   */
  default boolean hasFlag(FlagOption option, CommandSource source)
      throws CommandSyntaxException
  {
    var parsed = getParsedOption(option);

    if (parsed == null) {
      return false;
    }

    parsed.checkAccess(source);
    return true;
  }

  /**
   * Parse data about an option
   */
  interface ParsedOption {

    /**
     * Gets the string range that represents the start and end of this option's
     * input
     *
     * @return Option input range
     */
    StringRange range();

    /**
     * Gets the label that was used to identify the {@link #option()} during
     * parsing
     *
     * @return Parsed label
     */
    String usedLabel();

    /**
     * Gets the option
     * @return option
     */
    Option option();

    /**
     * Tests if the specified {@code source} is allowed to access this option.
     * <p>
     * Access rights are determined with {@link Option#test(CommandSource)}.
     * <p>
     * If the source is not allowed to access this option, an exception will be
     * thrown which will state that the user has input an unknown option, this
     * exception message will look identical to those thrown during option
     * parsing when the inputted option label belongs to an unknown option
     *
     * @param source Source to test
     * @throws CommandSyntaxException If the source cannot access this option
     */
    void checkAccess(CommandSource source) throws CommandSyntaxException;
  }

  /**
   * Parse data about a parsed value
   * @param <T> Value type
   */
  interface ParsedValue<T> extends ParsedOption {

    /**
     * Gets the argument
     * @return argument option
     */
    @Override
    ArgumentOption<T> option();

    /**
     * Gets the parsed value
     * @return parsed value
     */
    @NotNull
    T value();
  }
}
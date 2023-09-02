package net.forthecrown.grenadier.types.options;

import java.util.function.Consumer;

/**
 * Options argument builder
 */
public interface OptionsArgumentBuilder {

  /**
   * Adds a flag option
   *
   * @param option Option to add
   * @return this
   */
  OptionsArgumentBuilder addFlag(FlagOption option);

  /**
   * Adds all the specified {@code options} and makes them mutually exclusive
   * @param options Options to add
   * @return {@code this}
   */
  default OptionsArgumentBuilder oneOf(Option... options) {
    for (Option option : options) {
      addOptional(option, e -> e.exclusiveWith(options));
    }

    return this;
  }

  /**
   * Adds all the specified {@code options}, makes them mutually exclusive and required. Meaning
   * that one of the specified options will be required in the options input
   *
   * @param options Options to add
   * @return {@code this}
   */
  default OptionsArgumentBuilder requireOneOf(Option... options) {
    for (Option option : options) {
      addRequired(option, e -> e.exclusiveWith(options));
    }

    return this;
  }

  /**
   * Adds all the specified {@code options} and makes them all require each other.
   * <p>
   * This means that if 1 of the options is specified in the input, all the other options must be
   * specified as well
   *
   * @param options Options to add
   * @return {@code this}
   */
  default OptionsArgumentBuilder allOf(Option... options) {
    for (Option option : options) {
      addOptional(option, e -> e.requires(options));
    }

    return this;
  }

  /**
   * Adds all the specified {@code options} and makes them all require each other. All the
   * options will be made 'required' as well, meaning they MUST be given in the input, regardless
   * <p>
   * This means that if 1 of the options is specified in the input, all the other options must be
   * specified as well
   *
   * @param options Options to add
   * @return {@code this}
   */
  default OptionsArgumentBuilder requireAllOf(Option... options) {
    for (Option option : options) {
      addRequired(option, e -> e.requires(options));
    }

    return this;
  }

  /**
   * Adds an optional option
   * @param option Option to add
   * @param consumer Option configuration consumer
   * @return {@code this}
   */
  OptionsArgumentBuilder addOptional(Option option, Consumer<EntryBuilder> consumer);

  /**
   * Adds a required option, this option must be specified in the input
   * @param option Option to add
   * @param consumer Option configuration consumer
   * @return {@code this}
   */
  OptionsArgumentBuilder addRequired(Option option, Consumer<EntryBuilder> consumer);


  /**
   * Adds an optional option
   * @param option Option to add
   * @return {@code this}
   */
  OptionsArgumentBuilder addOptional(Option option);

  /**
   * Adds a required option, this option must be specified in the input
   * @param option Option to add
   * @return {@code this}
   */
  OptionsArgumentBuilder addRequired(Option option);

  /**
   * Creates the options argument
   *
   * @return Created argument
   * @throws IllegalArgumentException If no options were specified
   */
  OptionsArgument build() throws IllegalArgumentException;

  /**
   * Entry configuration builder
   */
  interface EntryBuilder {

    /**
     * Gets the option this entry represents
     * @return Option
     */
    Option option();

    /**
     * Sets the required options for this entry.
     * <p>
     * All the specified {@code options} must be present in the command input, or it will fail to
     * parse
     *
     * @param options Required options
     * @return {@code this}
     */
    EntryBuilder requires(Option... options);

    /**
     * Sets the mutually exclusive options for this entry
     * <p>
     * If any of the specified {@code options} are present in the command input, then it will fail
     * to parse
     *
     * @param options Mutually exclusive options
     * @return {@code this}
     */
    EntryBuilder exclusiveWith(Option... options);
  }
}

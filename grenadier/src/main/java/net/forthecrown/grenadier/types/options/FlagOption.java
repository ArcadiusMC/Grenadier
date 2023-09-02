package net.forthecrown.grenadier.types.options;

/**
 * An option that doesn't require a value.
 * <p>
 * Input example: {@code -flagName}
 */
public interface FlagOption extends Option {

  /**
   * Flag builder
   */
  interface Builder extends OptionBuilder<Builder> {

    /**
     * Builds the flag option
     * @return Created option
     * @throws IllegalArgumentException If an option label was not specified
     */
    FlagOption build() throws IllegalArgumentException;
  }
}
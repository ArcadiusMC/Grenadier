package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.types.options.OptionsArgumentImpl.BuilderImpl;
import org.jetbrains.annotations.Nullable;

/**
 * Argument type that parses a list of options
 * <p>
 * Input examples: <pre>
 * optionName=value
 * -flag
 * -flagName optionName = value
 * </pre>
 */
public interface OptionsArgument extends ArgumentType<ParsedOptions> {

  /**
   * Creates a new options argument builder
   * @return Created builder
   */
  static Builder builder() {
    return new BuilderImpl();
  }

  @Override
  ParsedOptions parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  /**
   * Gets an argument entry
   * @param option Option to get the entry for
   * @return Found entry, or {@code null}, if no entry exists for the
   *         provided option
   * @param <T> Option's type
   */
  @Nullable
  <T> ArgumentEntry<T> getEntry(ArgumentOption<T> option);

  /**
   * Gets an unmodifiable collection of all argument entries
   * @return Argument entries
   */
  Collection<ArgumentEntry<?>> getEntries();

  /**
   * Gets an unmodifiable set of all options added to this argument
   * @return Options
   */
  Set<Option> getOptions();

  /**
   * Gets an option in this argument by its label
   * @param label Option's label
   * @return Found option, or {@code null}, if no option was found
   */
  Option getOption(String label);

  /**
   * A single {@link ArgumentOption} entry
   * @param <T> Option's value type
   */
  interface ArgumentEntry<T> {

    /**
     * Gets the option
     * @return option
     */
    ArgumentOption<T> option();

    /**
     * Gets if a value for the {@link #option()} is required
     * @return {@code true} if this argument is required in parse input,
     *         {@code false} otherwise
     */
    boolean required();
  }

  /**
   * Options argument builder
   */
  interface Builder {

    /**
     * Adds a flag option
     * @param option Option to add
     * @return this
     */
    Builder addFlag(FlagOption option);

    /**
     * Adds an argument option
     * @param option Option to add
     * @param required {@code true}, if a value for the specified {@code option}
     *                 must always be given, {@code false} otherwise
     * @return this
     * @param <T> Option's value type
     */
    <T> Builder addOption(ArgumentOption<T> option, boolean required);

    /**
     * Delegate for {@link #addOption(ArgumentOption, boolean)} with
     * {@code false} for the 'required' parameter
     *
     * @see #addOption(ArgumentOption, boolean)
     */
    default <T> Builder addOptional(ArgumentOption<T> option) {
      return addOption(option, false);
    }

    /**
     * Delegate for {@link #addOption(ArgumentOption, boolean)} with
     * {@code true} for the 'required' parameter
     *
     * @see #addOption(ArgumentOption, boolean)
     */
    default <T> Builder addRequired(ArgumentOption<T> option) {
      return addOption(option, true);
    }

    /**
     * Creates the options argument
     * @return Created argument
     * @throws IllegalArgumentException If no options were specified
     */
    OptionsArgument build() throws IllegalArgumentException;
  }
}
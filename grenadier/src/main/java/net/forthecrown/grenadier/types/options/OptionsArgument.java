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
  static OptionsArgumentBuilder builder() {
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
   */
  @Nullable
  ArgumentEntry getEntry(Option option);

  /**
   * Gets an unmodifiable collection of all argument entries
   * @return Argument entries
   */
  Collection<ArgumentEntry> getEntries();

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
   */
  interface ArgumentEntry {

    /**
     * Gets the option
     * @return option
     */
    Option option();

    /**
     * Gets if a value for the {@link #option()} is required
     * @return {@code true} if this argument is required in parse input,
     *         {@code false} otherwise
     */
    boolean required();

    /**
     * Gets an unmodifiable set of options this entry is mutually exclusive with
     * @return Mutually exclusive options
     */
    Set<Option> exclusive();

    /**
     * Gets an unmodifiable set of options required by this entry
     * @return Required options
     */
    Set<Option> requires();
  }

}
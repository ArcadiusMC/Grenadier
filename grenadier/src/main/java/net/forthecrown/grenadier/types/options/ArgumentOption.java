package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Suggester;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An option that requires an argument value
 * <p>
 * Input example: {@code key=value}
 * @param <T> Value type
 */
public interface ArgumentOption<T> extends Option, Suggester<CommandSource> {

  /**
   * Gets the option's argument type.
   * <p>
   * This type is used to parse a value and also to suggest values
   *
   * @return Argument type
   */
  @NotNull
  ArgumentType<T> getArgumentType();

  /**
   * Gets the option's default value.
   * <p>
   * This value will be returned when {@link ParsedOptions} can't find the
   * parsed value of this option. In that case, the {@link #getCondition()}
   * result is ignored and this value will be returned.
   *
   * @return Default value, {@code null}, if no value was set
   */
  @Nullable
  T getDefaultValue();

  /**
   * Gets the suggestion provider for this option.
   * <p>
   * Determines the value suggestions for this option, if not set, uses the
   * suggestions provided by {@link #getArgumentType()}
   *
   * @return Suggestion provider, or {@code null}, if no suggestion provider
   *         was set
   */
  @Nullable
  Suggester<CommandSource> getSuggester();

  @Override
  default CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    var suggester = getSuggester();

    if (suggester == null) {
      return getArgumentType().listSuggestions(context, builder);
    }

    return suggester.getSuggestions(context ,builder);
  }

  /**
   * Argument option builder
   * @param <T> Argument type
   */
  interface Builder<T> extends OptionBuilder<Builder<T>> {

    /**
     * Sets the default value of the option
     * @param defaultValue default value
     * @return this
     * @see #getDefaultValue()
     */
    Builder<T> setDefaultValue(@Nullable T defaultValue);

    /**
     * Sets the suggestion provider. This will override the suggestions
     * returned by {@link #getArgumentType()}
     *
     * @param suggester Suggestion provider
     * @return this
     */
    Builder<T> setSuggester(@Nullable Suggester<CommandSource> suggester);

    /**
     * Creates the argument option
     * @return Created option
     * @throws IllegalArgumentException If an option label was not specified
     */
    ArgumentOption<T> build() throws IllegalArgumentException;
  }
}
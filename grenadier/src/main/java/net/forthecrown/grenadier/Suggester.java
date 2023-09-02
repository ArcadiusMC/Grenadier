package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

/**
 * An type of {@link SuggestionProvider} that is not allowed to throw
 * {@link com.mojang.brigadier.exceptions.CommandSyntaxException}s
 *
 * @param <S> Command source type
 */
public interface Suggester<S> extends SuggestionProvider<S> {

  /**
   * Wraps a suggestion provider and prevents it from throwing {@link CommandSyntaxException}s,
   * instead returning a set of empty suggestions when it throws
   *
   * @param provider Suggestion provider
   * @return Wrapped provider, or the provider itself, if it was already a suggester
   */
  static <S> Suggester<S> wrap(SuggestionProvider<S> provider) {
    if (provider instanceof Suggester<S>) {
      return (Suggester<S>) provider;
    }

    return (context, builder) -> {
      try {
        return provider.getSuggestions(context, builder);
      } catch (CommandSyntaxException exc) {
        return Suggestions.empty();
      }
    };
  }

  @Override
  CompletableFuture<Suggestions> getSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );
}
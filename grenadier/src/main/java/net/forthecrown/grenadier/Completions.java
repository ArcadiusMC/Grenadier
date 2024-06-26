package net.forthecrown.grenadier;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.forthecrown.grenadier.types.CoordinateSuggestion;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.generator.WorldInfo;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

/**
 * Utility class for command suggestions
 *
 * @see #suggest(SuggestionsBuilder, Iterable)
 * @see #suggest(SuggestionsBuilder, Stream)
 * @see #suggest(SuggestionsBuilder, String...)
 * @see #matches(String, String) Testing if command input matches a string
 * @see #matches(String, Key) Testing if a {@link Key} matches command input
 */
public final class Completions {
  private Completions() {}

  /**
   * Checks if the given string starts with the given token
   *
   * @param input Input given by the command source
   * @param suggestion Suggestion string
   * @return {@code true}, if {@code suggestion} starts with {@code input}
   */
  public static boolean matches(String input, String suggestion) {
    if (input.length() > suggestion.length()) {
      return false;
    }

    return suggestion.regionMatches(true, 0, input, 0, input.length());
  }

  /**
   * Tests if the given key is a valid suggestion for the given token
   *
   * @param input The command input to test against
   * @param key   The key to test
   * @return True, if the key is a valid suggestion, false otherwise
   */
  public static boolean matches(String input, Key key) {
    return matches(input, key.namespace())
        || matches(input, key.value())
        || matches(input, key.asString());
  }

  /**
   * Suggest all matching strings into the given SuggestionsBuilder
   *
   * @param builder     The builder to give suggestions to
   * @param suggestions The suggestions to pick from
   * @return The built suggestions
   */
  public static CompletableFuture<Suggestions> suggest(
      SuggestionsBuilder builder,
      Iterable<String> suggestions
  ) {
    return suggest(
        builder,
        suggestions instanceof Collection<String> strings
            ? strings.stream()
            : StreamSupport.stream(suggestions.spliterator(), false)
    );
  }

  /**
   * Suggest all matching strings into the given SuggestionsBuilder
   *
   * @param builder     The builder to give suggestions to
   * @param suggestions The suggestions to pick from
   * @return The built suggestions
   */
  public static CompletableFuture<Suggestions> suggest(
      SuggestionsBuilder builder,
      String... suggestions
  ) {
    return suggest(builder, Arrays.stream(suggestions));
  }

  /**
   * Suggest all matching strings into the given SuggestionsBuilder
   *
   * @param builder     The builder to give suggestions to
   * @param suggestions The suggestions to pick from
   * @return The built suggestions
   */
  public static CompletableFuture<Suggestions> suggest(
      SuggestionsBuilder builder,
      Stream<String> suggestions
  ) {
    String token = builder.getRemainingLowerCase();

    suggestions
        .filter(s -> matches(token, s))
        .forEach(builder::suggest);

    return builder.buildFuture();
  }

  /**
   * Suggests all input-matching keyed objects in the specified
   * {@code iterable}.
   * <p>
   * {@link #matches(String, Key)} is used to test if a key matches the
   * {@code builder}'s input
   *
   * @param builder Builder to suggest to
   * @param iterable Keys to suggest
   * @return suggestions
   */
  public static CompletableFuture<Suggestions> suggestKeyed(
      SuggestionsBuilder builder,
      Iterable<? extends Keyed> iterable
  ) {
    var token = builder.getRemainingLowerCase();

    for (var k: iterable) {
      if (!matches(token, k.getKey())) {
        continue;
      }

      builder.suggest(k.getKey().asString());
    }

    return builder.buildFuture();
  }

  public static CompletableFuture<Suggestions> suggestKeys(
      SuggestionsBuilder builder,
      Iterable<? extends Key> iterable
  ) {
    var token = builder.getRemainingLowerCase();

    for (var k: iterable) {
      if (!matches(token, k)) {
        continue;
      }

      builder.suggest(k.asString());
    }

    return builder.buildFuture();
  }

  public static CompletableFuture<Suggestions> suggestKeys(
      SuggestionsBuilder builder,
      Stream<? extends Key> stream
  ) {
    String token = builder.getRemainingLowerCase();

    stream.filter(key -> matches(token, key))
        .map(Key::asString)
        .forEach(builder::suggest);

    return builder.buildFuture();
  }

  /**
   * Suggests all currently loaded worlds
   * @param builder Builder to suggest to
   * @return suggestions
   */
  public static CompletableFuture<Suggestions> suggestWorlds(
      SuggestionsBuilder builder
  ) {
    return suggest(builder, Bukkit.getWorlds().stream().map(WorldInfo::getName));
  }

  /**
   * Suggests scoreboard objectives
   * @param builder Builder to suggest to
   * @return suggestions
   */
  public static CompletableFuture<Suggestions> suggestObjectives(
      SuggestionsBuilder builder
  ) {
    return suggest(
        builder,
        Bukkit.getScoreboardManager()
            .getMainScoreboard()
            .getObjectives()
            .stream()
            .map(Objective::getName)
    );
  }

  /**
   * Suggests scoreboard team names
   * @param builder Builder to suggest to
   * @return suggestions
   */
  public static CompletableFuture<Suggestions> suggestTeams(
      SuggestionsBuilder builder
  ) {
    return suggest(
        builder,
        Bukkit.getScoreboardManager()
            .getMainScoreboard()
            .getTeams()
            .stream()
            .map(Team::getName)
    );
  }

  /**
   * Suggests a collection of coordinate suggestions
   * @param builder Builder to suggest to
   * @param suggestions Suggestions
   * @return suggestions
   */
  public static CompletableFuture<Suggestions> suggestCoordinates(
      SuggestionsBuilder builder,
      Collection<CoordinateSuggestion> suggestions
  ) {
    suggestions.forEach(s -> s.applySuggestions(builder));
    return builder.buildFuture();
  }

  /**
   * Combines several suggestion providers into 1 by merging all their outputs using
   * {@link Suggestions#merge(String, Collection)}
   *
   * @param providers Providers to merge
   * @return Merged providers
   */
  @SafeVarargs
  public static <S> SuggestionProvider<S> combine(SuggestionProvider<S>... providers) {
    Objects.requireNonNull(providers, "Null providers");
    Validate.noNullElements(providers);
    return combine(Arrays.asList(providers));
  }

  /**
   * Combines several suggestion providers into 1 by merging all their outputs using
   * {@link Suggestions#merge(String, Collection)}
   *
   * @param providers Providers to merge
   * @return Merged providers
   */
  public static <S> SuggestionProvider<S> combine(Collection<SuggestionProvider<S>> providers) {
    Objects.requireNonNull(providers, "Null providers");

    if (providers.isEmpty()) {
      return (context, builder) -> Suggestions.empty();
    }

    if (providers.size() == 1) {
      return providers.iterator().next();
    }

    return (context, builder) -> {
      CompletableFuture<Suggestions> future = Suggestions.empty();
      String input = context.getInput();

      for (SuggestionProvider<S> provider : providers) {
        CompletableFuture<Suggestions> providerFuture = provider.getSuggestions(context, builder);

        future = providerFuture.thenCombine(future, (suggestions, suggestions2) -> {
          return Suggestions.merge(input, List.of(suggestions, suggestions2));
        });
      }

      return future;
    };
  }
}
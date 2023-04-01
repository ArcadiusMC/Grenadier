package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef;

@RequiredArgsConstructor
class CompiledSuggester implements SuggestionProvider<CommandSource> {

  private static final Class<?>[] PARAMS = {
      CommandContext.class,
      SuggestionsBuilder.class
  };

  private final ClassComponentRef ref;
  private final Object commandClass;

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) throws CommandSyntaxException {

    return ref.execute(
        CompletableFuture.class,
        SuggestionProvider.class,
        suggestionProvider -> suggestionProvider.getSuggestions(context,
            builder),
        PARAMS,
        commandClass,

        context, builder
    );
  }
}
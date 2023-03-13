package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

public interface EntityArgument extends ArgumentType<EntitySelector> {

  boolean allowsMultiple();

  boolean includesEntities();

  @Override
  default EntitySelector parse(StringReader reader)
      throws CommandSyntaxException
  {
    return parse(reader, false);
  }

  EntitySelector parse(StringReader reader, boolean overridePermissions)
      throws CommandSyntaxException;


  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );
}
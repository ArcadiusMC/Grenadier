package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.types.LocalDateArgument;

public class LocalDateArgumentImpl implements LocalDateArgument {

  @Override
  public LocalDate parse(StringReader reader) throws CommandSyntaxException {
    return null;
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context, SuggestionsBuilder builder
  ) {
    return null;
  }
}
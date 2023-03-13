package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.GameModeArgument;
import org.bukkit.GameMode;

public class GameModeArgumentImpl implements GameModeArgument {

  @Override
  public GameMode parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();
    String word = reader.readUnquotedString();

    GameMode gameMode = BY_LABEL.get(word);

    if (gameMode == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownGamemode(word, reader);
    }

    return gameMode;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context, SuggestionsBuilder builder
  ) {
    return Completions.suggest(
        builder,
        Arrays.stream(GameMode.values())
            .map(gameMode -> gameMode.name().toLowerCase())
    );
  }
}
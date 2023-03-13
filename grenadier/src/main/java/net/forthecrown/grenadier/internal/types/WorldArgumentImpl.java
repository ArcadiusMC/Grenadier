package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.utils.Readers;
import net.forthecrown.grenadier.types.WorldArgument;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldArgumentImpl implements WorldArgument {

  @Override
  public World parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();
    String word = Readers.readUntilWhitespace(reader);
    World world = Bukkit.getWorld(word);

    if (world == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownWorld(word, reader);
    }

    return world;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context, SuggestionsBuilder builder
  ) {
    return Completions.suggestWorlds(builder);
  }
}
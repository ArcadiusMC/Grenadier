package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

class WorldArgumentImpl implements WorldArgument {

  static final WorldArgument INSTANCE = new WorldArgumentImpl();

  @Override
  public World parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();
    String word = KeyArgumentImpl.readKeyString(reader);

    World world;

    if (word.contains(":")) {
      reader.setCursor(start);
      NamespacedKey key = ArgumentTypes.key().parse(reader);

      world = Bukkit.getWorld(key);
    } else {
      world = Bukkit.getWorld(word);
    }

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
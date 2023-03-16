package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import org.bukkit.World;

/**
 * Parses a world by its name or by its namespaced key.
 * <p>
 * Input examples: <pre>
 * world
 * world_the_end
 * minecraft:overworld
 * minecraft:nether
 * </pre>
 */
public interface WorldArgument extends ArgumentType<World> {

  @Override
  World parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );
}
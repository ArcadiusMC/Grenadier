package net.forthecrown.grenadier.types;

import static java.util.Map.entry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.GameMode;

/**
 * Argument type that parses a gamemode.
 * <p>
 * Valid inputs: <pre>
 * - survival: 'survival', 's', '0'
 * - creative: 'creative', 'c', '1'
 * - adventure: 'adventure', 'a', '3'
 * - spectator: 'spectator', 'sp', '2'
 * </pre>
 */
public interface GameModeArgument extends ArgumentType<GameMode> {

  /**
   * Label to Gamemode map. This map is used to parse the gamemode from
   * player input
   */
  Map<String, GameMode> BY_LABEL = Map.ofEntries(
      entry("0",          GameMode.SURVIVAL),
      entry("s",          GameMode.SURVIVAL),
      entry("survival",   GameMode.SURVIVAL),

      entry("1",          GameMode.CREATIVE),
      entry("c",          GameMode.CREATIVE),
      entry("creative",   GameMode.CREATIVE),

      entry("2",          GameMode.ADVENTURE),
      entry("a",          GameMode.ADVENTURE),
      entry("adventure",  GameMode.ADVENTURE),

      entry("3",          GameMode.ADVENTURE),
      entry("sp",         GameMode.ADVENTURE),
      entry("spectator",  GameMode.ADVENTURE)
  );

  @Override
  GameMode parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );
}
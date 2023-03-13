package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.TeamArgument;
import net.minecraft.commands.CommandBuildContext;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

public class TeamArgumentImpl
    implements TeamArgument, VanillaMappedArgument
{

  @Override
  public Team parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();
    String word = reader.readUnquotedString();

    Team team = Bukkit.getScoreboardManager()
        .getMainScoreboard()
        .getTeam(word);

    if (team == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownTeam(word, reader);
    }

    return team;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggestTeams(builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.TeamArgument.team();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}
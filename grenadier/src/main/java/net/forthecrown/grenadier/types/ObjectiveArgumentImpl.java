package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;

class ObjectiveArgumentImpl
    implements ObjectiveArgument, VanillaMappedArgument
{

  static final ObjectiveArgument INSTANCE = new ObjectiveArgumentImpl();

  @Override
  public Objective parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();
    String word = reader.readUnquotedString();

    Objective objective = Bukkit.getScoreboardManager()
        .getMainScoreboard()
        .getObjective(word);

    if (objective == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownObjective(word, reader);
    }

    return objective;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggestObjectives(builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.ObjectiveArgument.objective();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}
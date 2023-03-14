package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;

class TimeArgumentImpl implements TimeArgument, VanillaMappedArgument {

  static final TimeArgument INSTANCE = new TimeArgumentImpl();

  @Override
  public Duration parse(StringReader reader) throws CommandSyntaxException {
    double initialTime = reader.readDouble();

    if (!reader.canRead() || Character.isWhitespace(reader.peek())) {
      return Duration.ofMillis((long) initialTime);
    }

    final int start = reader.getCursor();
    String word = reader.readUnquotedString();

    Unit unit = Unit.LOOKUP.get(word);

    if (unit == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().invalidTimeUnit(word, reader);
    }

    return Duration.ofMillis(
        (long) (unit.getMillis() * initialTime)
    );
  }

  private static final List<String> SUGGESTIONS
      = List.of("t", "s", "m", "h", "d", "w", "mo", "yr");

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    String before = builder.getRemaining().toLowerCase();
    String after = before.replaceAll("[^\\d.]", "");

    if (after.isBlank()) {
      return Completions.suggest(
          builder,
          SUGGESTIONS.stream().map(s -> "10" + s)
      );
    }

    return Completions.suggest(
        builder,
        SUGGESTIONS.stream().map(s -> after + s)
    );
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.word();
  }
}
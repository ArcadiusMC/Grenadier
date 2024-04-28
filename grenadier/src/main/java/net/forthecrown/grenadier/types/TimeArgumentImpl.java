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
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;

class TimeArgumentImpl implements TimeArgument, VanillaMappedArgument {

  private static final List<String> SUGGESTIONS = List.of("t", "s", "m", "h", "d", "w", "mo", "yr");

  private static final long MILLIS_IN_NANOS = 1000000L;

  static final TimeArgument INSTANCE = new TimeArgumentImpl();

  @Override
  public Duration parse(StringReader reader) throws CommandSyntaxException {
    DurationParser<?> parser = new DurationParser<>(reader);
    double timeInMillis = parser.parseNumber();
    double nanoTime = timeInMillis * MILLIS_IN_NANOS;
    return Duration.ofNanos((long) nanoTime);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    StringReader reader = Readers.forSuggestions(builder);
    DurationParser<S> parser = new DurationParser<>(reader);
    return parser.suggestTo(context, builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.word();
  }

  private static class DurationParser<S> extends UnitParser<S> {

    public DurationParser(StringReader reader) {
      super(reader);
    }

    @Override
    protected void suggestInitial(SuggestionsBuilder builder) {
      Completions.suggest(builder, SUGGESTIONS.stream().map(s -> "10" + s));
    }

    @Override
    protected void suggestUnits(SuggestionsBuilder builder) {
      String token = builder.getRemainingLowerCase();

      if (token.isEmpty()) {
        Completions.suggest(builder, SUGGESTIONS);
        return;
      }

      int tokenLen = token.length();

      for (Unit value : Unit.values()) {
        for (String string : value.getStrings()) {
          if (!Completions.matches(token, string)) {
            continue;
          }

          if (string.length() > 2 && tokenLen < 3) {
            continue;
          }

          builder.suggest(string);
        }
      }
    }

    @Override
    protected OptionalDouble getMultiplier(String unitName) {
      Unit unit = Unit.LOOKUP.get(unitName);

      if (unit == null) {
        return OptionalDouble.empty();
      }

      return OptionalDouble.of(unit.getMillis());
    }
  }
}
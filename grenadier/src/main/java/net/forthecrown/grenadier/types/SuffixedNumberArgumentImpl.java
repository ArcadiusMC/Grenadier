package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;

class SuffixedNumberArgumentImpl<N extends Number>
    implements SuffixedNumberArgument<N>
{

  private final Map<String, N> suffixes;
  private final NumberType<N> type;

  private final N min;
  private final N max;

  public SuffixedNumberArgumentImpl(
      Map<String, N> suffixes,
      NumberType<N> type,
      N min,
      N max
  ) {
    this.suffixes = new HashMap<>(suffixes);
    this.type = type;
    this.min = min;
    this.max = max;
  }

  @Override
  public Map<String, N> getSuffixes() {
    return Collections.unmodifiableMap(suffixes);
  }

  @Override
  public N parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();

    SuffixedParser<?> parser = new SuffixedParser<>(reader);
    double value = parser.parseNumber();

    N val = type.fromDouble(value);
    validateSize(val, reader, start);

    return val;
  }

  void validateSize(N value, StringReader reader, int start)
      throws CommandSyntaxException
  {
    double val = value.doubleValue();
    double minVal = min.doubleValue();
    double maxVal = max.doubleValue();

    if (val < minVal) {
      reader.setCursor(start);
      throw type.tooLow(reader, min, value);
    }

    if (val > maxVal) {
      reader.setCursor(start);
      throw type.tooHigh(reader, max, value);
    }
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    StringReader reader = Readers.forSuggestions(builder);
    SuffixedParser<S> parser = new SuffixedParser<>(reader);
    return parser.suggestTo(context, builder);
  }

  interface NumberType<N> {

    NumberType<Double> DOUBLE = new NumberType<>() {
      @Override
      public Double fromDouble(double d) {
        return d;
      }

      @Override
      public CommandSyntaxException tooLow(StringReader reader, Double min, Double value) {
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .doubleTooLow()
            .createWithContext(reader, value, min);
      }

      @Override
      public CommandSyntaxException tooHigh(StringReader reader, Double max, Double value) {
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .doubleTooHigh()
            .createWithContext(reader, value, max);
      }
    };

    NumberType<Integer> INT = new NumberType<>() {
      @Override
      public Integer fromDouble(double d) {
        return (int) d;
      }

      @Override
      public CommandSyntaxException tooLow(StringReader reader, Integer min, Integer value) {
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .integerTooLow()
            .createWithContext(reader, value, min);
      }

      @Override
      public CommandSyntaxException tooHigh(StringReader reader, Integer max, Integer value) {
        return CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .integerTooHigh()
            .createWithContext(reader, value, max);
      }
    };

    N fromDouble(double d);

    CommandSyntaxException tooLow(StringReader reader, N min, N value);

    CommandSyntaxException tooHigh(StringReader reader, N max, N value);
  }

  class SuffixedParser<S> extends UnitParser<S> {

    public SuffixedParser(StringReader reader) {
      super(reader);
    }

    @Override
    protected void suggestInitial(SuggestionsBuilder builder) {
      Completions.suggest(builder,
          suffixes.keySet()
              .stream()
              .map(string -> "10" + string)
      );
    }

    @Override
    protected void suggestUnits(SuggestionsBuilder builder) {
      Completions.suggest(builder, suffixes.keySet());
    }

    @Override
    protected OptionalDouble getMultiplier(String unitName) {
      N value = suffixes.get(unitName);
      if (value == null) {
        return OptionalDouble.empty();
      }
      return OptionalDouble.of(value.doubleValue());
    }
  }
}
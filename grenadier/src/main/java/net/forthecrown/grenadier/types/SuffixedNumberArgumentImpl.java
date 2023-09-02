package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.HashMap;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
    final int start = reader.getCursor();
    double number = reader.readDouble();

    if (!reader.canRead() || Character.isWhitespace(reader.peek())) {
      N result = type.fromDouble(number);

      validateSize(result, reader, start);
      return result;
    }

    String word = reader.readUnquotedString();
    N multiplier = suffixes.get(word);

    if (multiplier == null) {
      throw Grenadier.exceptions().unknownSuffix(reader, word);
    }

    N finalValue = type.fromDouble(number * multiplier.doubleValue());
    validateSize(finalValue, reader, start);

    return finalValue;
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
  public CompletableFuture<Suggestions> listSuggestions(
      CommandContext context,
      SuggestionsBuilder builder
  ) {
    StringReader reader = Readers.forSuggestions(builder);
    final int start = reader.getCursor();

    while (reader.canRead() && StringReader.isAllowedNumber(reader.peek())) {
      reader.skip();
    }

    final int end = reader.getCursor();
    String suggestionsPrefix;

    if (start == end) {
      suggestionsPrefix = "10";
    } else {
      suggestionsPrefix = reader.getString().substring(start, end);
    }

    return Completions.suggest(builder,
        suffixes.keySet()
            .stream()
            .map(s -> suggestionsPrefix + s)
    );
  }

  public interface NumberType<N> {

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
}
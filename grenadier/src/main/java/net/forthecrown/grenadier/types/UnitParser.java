package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.chars.CharList;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Suggester;

abstract class UnitParser<S> implements Suggester<S> {

  static final CharList JOINER_CHARS = CharList.of(';', '+', '-');

  static final Consumer<SuggestionsBuilder> JOINER = builder -> {
    Completions.suggest(builder, "+", "-");
  };

  private final StringReader reader;

  private int suggestOffset;
  private Consumer<SuggestionsBuilder> suggestions;

  public UnitParser(StringReader reader) {
    this.reader = reader;
  }

  protected abstract void suggestInitial(SuggestionsBuilder builder);
  protected abstract void suggestUnits(SuggestionsBuilder builder);

  protected abstract OptionalDouble getMultiplier(String unitName);

  void setSuggestions(Consumer<SuggestionsBuilder>... consumer) {
    setSuggestions(reader.getCursor(), consumer);
  }

  void setSuggestions(int off, Consumer<SuggestionsBuilder>... consumers) {
    suggestOffset = off;

    if (consumers.length == 1) {
      this.suggestions = consumers[0];
    } else {
      this.suggestions = builder -> {
        for (Consumer<SuggestionsBuilder> consumer : consumers) {
          consumer.accept(builder);
        }
      };
    }
  }

  double parseNumber() throws CommandSyntaxException {
    setSuggestions(this::suggestInitial);

    double value = readDouble();
    setSuggestions(this::suggestUnits, JOINER);

    if (reader.canRead() && isIdentifier(reader.peek())) {
      int start = reader.getCursor();

      String unitName = readIdentifier();
      OptionalDouble unit = getMultiplier(unitName.toLowerCase());

      if (unit.isEmpty()) {
        reader.setCursor(start);
        throw Grenadier.exceptions().invalidTimeUnit(unitName, reader);
      }

      value *= unit.getAsDouble();
      setSuggestions(JOINER);
    }

    if (reader.canRead() && JOINER_CHARS.contains(reader.peek())) {
      char ch = reader.peek();
      reader.skip();

      double millis = parseNumber();

      if (ch == '+' || ch == ';') {
        value += millis;
      } else {
        value -= millis;
      }
    }

    return value;
  }

  String readIdentifier() {
    int start = reader.getCursor();
    while (reader.canRead() && isIdentifier(reader.peek())) {
      reader.skip();
    }

    return reader.getString().substring(start, reader.getCursor());
  }

  static boolean isIdentifier(char ch) {
    return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
  }

  double readDouble() throws CommandSyntaxException {
    if (!reader.canRead()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerExpectedDouble()
          .createWithContext(reader);
    }

    int start = reader.getCursor();

    if (reader.peek() == '-') {
      reader.skip();
    }

    while (reader.canRead() && isAllowedInNumber(reader.peek())) {
      reader.skip();
    }

    String substr = reader.getString().substring(start, reader.getCursor());

    if (substr.isEmpty()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerExpectedDouble()
          .createWithContext(reader);
    }

    try {
      return Double.parseDouble(substr);
    } catch (NumberFormatException exc) {
      reader.setCursor(start);
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .readerInvalidDouble()
          .createWithContext(reader, substr);
    }
  }

  static boolean isAllowedInNumber(char ch) {
    return (ch >= '0' && ch <= '9') || ch == '.';
  }

  public CompletableFuture<Suggestions> suggestTo(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    try {
      parseNumber();
    } catch (CommandSyntaxException exc) {
      // Ignored
    }

    return getSuggestions(context, builder);
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (suggestOffset != builder.getStart()) {
      builder = builder.createOffset(suggestOffset);
    }

    if (suggestions == null) {
      suggestInitial(builder);
    } else {
      suggestions.accept(builder);
    }

    return builder.buildFuture();
  }
}

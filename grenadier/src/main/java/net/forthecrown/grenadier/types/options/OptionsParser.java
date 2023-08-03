package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Suggester;

@Getter
class OptionsParser implements Suggester<CommandSource> {

  private final StringReader reader;
  private final ParsedOptionsImpl options;
  private final OptionsArgumentImpl argument;

  private Suggester<CommandSource> suggester;

  public OptionsParser(StringReader reader, OptionsArgumentImpl argument) {
    this.argument = argument;
    this.reader = reader;
    this.options = new ParsedOptionsImpl(reader.getString());
  }

  public void parse() throws CommandSyntaxException {
    suggest(reader.getCursor(), suggestLabels());

    while (true) {
      reader.skipWhitespace();
      parseOption();

      if (reader.canRead()) {
        if (!Character.isWhitespace(reader.peek())) {
          throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
              .dispatcherExpectedArgumentSeparator()
              .createWithContext(reader);
        }
      } else {
        break;
      }
    }

    for (var e: argument.getEntries()) {
      if (!e.required() || options.has(e.option())) {
        continue;
      }

      throw Grenadier.exceptions().missingOption(e.option());
    }
  }

  private void parseOption() throws CommandSyntaxException {
    final int start = reader.getCursor();
    suggest(start, suggestLabels());

    String word = reader.readUnquotedString();

    if (word.isBlank()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .dispatcherUnknownArgument()
          .createWithContext(reader);
    }

    Option option = findOption(word);

    if (option == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownOption(reader, word);
    }

    if (option instanceof FlagOption flag) {
      if (options.has(flag)) {
        reader.setCursor(start);
        throw Grenadier.exceptions().flagAlreadySet(word, reader);
      }

      StringRange range = StringRange.between(start, reader.getCursor());
      options.addFlag(flag, word, range);
    } else {
      ArgumentOption<Object> arg = (ArgumentOption<Object>) option;

      reader.skipWhitespace();
      final int c = reader.getCursor();

      suggest(c, separator());

      reader.expect('=');
      reader.skipWhitespace();

      suggest(reader.getCursor(), arg);

      if (options.has(arg)) {
        reader.setCursor(start);
        throw Grenadier.exceptions().optionAlreadySet(word, reader);
      }

      Object value = arg.getArgumentType().parse(reader);

      if (reader.canRead() && !Character.isWhitespace(reader.peek())) {
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .dispatcherExpectedArgumentSeparator()
            .createWithContext(reader);
      }

      StringRange range = StringRange.between(start, reader.getCursor());
      options.addValue(arg, value, word, range);
    }
  }

  private Option findOption(String label) {
    Option option = argument.getOption(label);

    if (option == null && label.startsWith("-")) {
      label = label.substring(1);
      option = argument.getOption(label);
    }

    return option;
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    if (suggester == null) {
      suggestLabels().apply(builder, context.getSource());
      return builder.buildFuture();
    }

    return suggester.getSuggestions(context, builder);
  }

  private void suggest(int cursor, ArgumentOption<?> type) {
    this.suggester = (context, builder) -> {
      if (cursor != builder.getStart()) {
        builder = builder.createOffset(cursor);
      }

      return type.getSuggestions(context, builder);
    };
  }

  private void suggest(int cursor, SuggestionConsumer consumer) {
    this.suggester = (context, builder) -> {
      if (builder.getStart() != cursor) {
        builder = builder.createOffset(cursor);
      }

      consumer.apply(builder, context.getSource());

      return builder.buildFuture();
    };
  }

  private SuggestionConsumer separator() {
    return (builder, source) -> Completions.suggest(builder, "=");
  }

  private SuggestionConsumer suggestLabels() {
    return (builder, source) -> {
      var remaining = builder.getRemainingLowerCase();

      outer: for (var o: argument.getOptions()) {
        if (!o.test(source) || options.has(o)) {
          continue;
        }

        // Filter this option out if we've already parsed an exclusive option
        if (o instanceof ArgumentOption<?> arg) {
          for (ArgumentOption<?> excl : arg.getMutuallyExclusive()) {
            if (!options.has(excl)) {
              continue;
            }

            continue outer;
          }
        }

        String prefix = o instanceof FlagOption ? "-" : "";

        for (var l: o.getLabels()) {
          if (!Completions.matches(remaining, l)) {
            continue;
          }

          if (o.getTooltip() == null) {
            builder.suggest(prefix + l);
          } else {
            builder.suggest(prefix + l, Grenadier.toMessage(o.getTooltip()));
          }
        }
      }
    };
  }

  interface SuggestionConsumer {
    void apply(SuggestionsBuilder builder, CommandSource source);
  }
}
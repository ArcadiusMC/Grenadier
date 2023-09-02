package net.forthecrown.grenadier.types.options;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.grenadier.types.options.OptionsArgumentImpl.Entry;

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

      if (reader.canRead() && !StringReader.isAllowedInUnquotedString(reader.peek())) {
        break;
      }

      parseOption();

      if (reader.canRead()) {
        if (!Character.isWhitespace(reader.peek())) {
          throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
              .dispatcherExpectedArgumentSeparator()
              .createWithContext(reader);
        }

        suggest(reader.getCursor() + 1, suggestLabels());
      } else {
        break;
      }
    }

    validate();
  }

  private void validate() throws CommandSyntaxException {
    ImmutableSet<Entry> entries = argument.options;
    Set<Option> validatedRequired = new HashSet<>();

    outer:
    for (Entry entry : entries) {
      if (!entry.required()) {
        continue;
      }

      var o = entry.option();

      if (validatedRequired.contains(o)) {
        continue;
      }

      if (options.has(o)) {
        validatedRequired.addAll(entry.exclusive());
        validatedRequired.add(o);
        continue;
      }

      for (Option option : entry.exclusive()) {
        if (options.has(option)) {
          validatedRequired.addAll(entry.exclusive());
          validatedRequired.add(o);
          continue outer;
        }
      }

      Set<Option> requires = new HashSet<>(entry.requires());
      requires.removeIf(options::has);

      throw Grenadier.exceptions().missingOption(o, entry.exclusive(), requires);
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

    Entry entry = findOption(word);

    if (entry == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownOption(reader, word);
    }

    Option option = entry.option();

    if (option instanceof FlagOption flag) {
      if (options.has(flag)) {
        reader.setCursor(start);
        throw Grenadier.exceptions().flagAlreadySet(word, reader);
      }

      StringRange range = StringRange.between(start, reader.getCursor());
      options.addFlag(entry, word, range);
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

      if (!reader.canRead()) {
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .dispatcherParseException()
            .createWithContext(reader, "?");
      }

      Object value = arg.getArgumentType().parse(reader);

      if (reader.canRead() && !Character.isWhitespace(reader.peek())) {
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .dispatcherExpectedArgumentSeparator()
            .createWithContext(reader);
      }

      StringRange range = StringRange.between(start, reader.getCursor());
      options.addValue(entry, value, word, range);
    }
  }

  private Entry findOption(String label) {
    Entry option = argument.optionLookup.get(label);

    if (option == null && label.startsWith("-")) {
      label = label.substring(1);
      option = argument.optionLookup.get(label);
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

      outer: for (var e: argument.options) {
        var o = e.option();

        if (!o.test(source) || options.has(o)) {
          continue;
        }

        // Filter this option out if we've already parsed an exclusive option
        for (Option excl : e.exclusive()) {
          if (!options.has(excl)) {
            continue;
          }

          continue outer;
        }

        String prefix = o instanceof FlagOption ? "-" : "";
        var label = prefix + o.getLabel();

        if (!Completions.matches(remaining, label)) {
          continue;
        }

        if (o.getTooltip() == null) {
          builder.suggest(label);
        } else {
          builder.suggest(label, Grenadier.toMessage(o.getTooltip()));
        }
      }
    };
  }

  interface SuggestionConsumer {
    void apply(SuggestionsBuilder builder, CommandSource source);
  }
}
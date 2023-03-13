package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.utils.Readers;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.minecraft.commands.CommandBuildContext;

public class ArrayArgumentImpl<T>
    implements ArrayArgument<T>, VanillaMappedArgument
{

  private final ArgumentType<T> listType;

  public ArrayArgumentImpl(ArgumentType<T> listType) {
    this.listType = Objects.requireNonNull(listType);
  }

  @Override
  public ArgumentType<T> listType() {
    return listType;
  }

  @Override
  public List<T> parse(StringReader reader) throws CommandSyntaxException {
    ArrayParser<?> parser = new ArrayParser<>(reader);
    parser.parse();
    return parser.list;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    StringReader reader = Readers.forSuggestions(builder);
    ArrayParser<S> parser = new ArrayParser<>(reader);

    try {
      parser.parse();
    } catch (CommandSyntaxException ignored) {}

    return parser.getSuggestions(context, builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.greedyString();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return false;
  }

  @RequiredArgsConstructor
  class ArrayParser<S> implements Suggester<S> {
    final List<T> list = new ArrayList<>();

    final StringReader reader;
    boolean suggestSeparator = false;
    int lastSuggestionStart;

    void parse() throws CommandSyntaxException {
      lastSuggestionStart = reader.getCursor();

      while (true) {
        suggestSeparator = false;
        lastSuggestionStart = reader.getCursor();

        T value = listType.parse(reader);
        list.add(value);

        suggestSeparator = true;
        lastSuggestionStart = reader.getCursor();

        if (!reader.canRead()) {
          break;
        }

        if (Character.isWhitespace(reader.peek())) {
          final int beforeSkip = reader.getCursor();
          reader.skipWhitespace();

          if (!reader.canRead()) {
            break;
          }

          if (reader.peek() == ',') {
            continue;
          }

          reader.setCursor(beforeSkip);
          break;
        }

        if (reader.peek() == ',') {
          reader.skip();
        } else {
          throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
              .dispatcherExpectedArgumentSeparator()
              .createWithContext(reader);
        }
      }
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<S> context,
        SuggestionsBuilder builder
    ) {
      if (lastSuggestionStart != builder.getStart()) {
        builder = builder.createOffset(lastSuggestionStart);
      }

      if (suggestSeparator) {
        return Completions.suggest(builder, ",");
      }

      return listType.listSuggestions(context, builder);
    }
  }
}
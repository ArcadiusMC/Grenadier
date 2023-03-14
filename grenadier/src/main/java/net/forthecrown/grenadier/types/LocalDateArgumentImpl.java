package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.utils.Readers;
import net.minecraft.commands.CommandBuildContext;

class LocalDateArgumentImpl
    implements LocalDateArgument, VanillaMappedArgument
{

  static final LocalDateArgumentImpl INSTANCE = new LocalDateArgumentImpl();

  @Override
  public LocalDate parse(StringReader reader) throws CommandSyntaxException {
    DateParser parser = new DateParser(reader);
    parser.parse();
    return parser.toLocalDate();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    StringReader reader = Readers.forSuggestions(builder);
    DateParser parser = new DateParser(reader);

    try {
      parser.parse();
    } catch (CommandSyntaxException exc) {
      // Ignored
    }

    return parser.suggest(builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.word();
  }

  static class DateParser {
    private int day = 0;
    private int month = 0;
    private int year = 0;

    private final StringReader reader;

    public DateParser(StringReader reader) {
      this.reader = reader;
    }

    public void parse() throws CommandSyntaxException {
      final int start = reader.getCursor();

      day = Readers.readPositiveInt(reader, 1, 31);
      separator();
      month = Readers.readPositiveInt(reader, 1, 12);
      separator();
      year = Readers.readPositiveInt(reader, Year.MIN_VALUE, Year.MAX_VALUE);

      Year yearObj = Year.of(year);
      Month monthObj = Month.of(month);
      int monthLength = monthObj.length(yearObj.isLeap());

      if (day > monthLength) {
        reader.setCursor(start);

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .integerTooHigh()
            .createWithContext(reader, day, monthLength);
      }
    }

    private LocalDate toLocalDate() {
      return LocalDate.of(year, month, day);
    }

    private void separator() throws CommandSyntaxException {
      reader.expect('-');
    }

    public CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder) {
      LocalDate now = LocalDate.now();

      if (day > 0) {
        now = now.withDayOfMonth(day);
      }

      if (month > 0) {
        now = now.withMonth(month);
      }

      if (year != 0) {
        now = now.withYear(year);
      }

      return Completions.suggest(builder, now.toString());
    }
  }
}
package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParsedOptions {

  @Nullable
  ParsedOption getParsedOption(@NotNull Option option);

  @Nullable
  ParsedOption getParsedOption(@NotNull String label);

  @Nullable
  default  <T> ParsedValue<T> getParsedValue(ArgumentOption<T> option) {
    return (ParsedValue<T>) getParsedOption(option);
  }

  default boolean has(Option option) {
    return getParsedOption(option) != null;
  }

  @Nullable
  default <T> T getValue(ArgumentOption<T> option) {
    var parsedValue = getParsedValue(option);

    if (parsedValue == null) {
      return null;
    }

    return parsedValue.value();
  }

  default <T> T getValue(ArgumentOption<T> option, CommandSource source)
      throws CommandSyntaxException
  {
    var parsedValue = getParsedValue(option);

    if (parsedValue == null) {
      return null;
    }

    parsedValue.checkAccess(source);
    return parsedValue.value();
  }

  default boolean hasFlag(FlagOption option, CommandSource source)
      throws CommandSyntaxException
  {
    var parsed = getParsedOption(option);

    if (parsed == null) {
      return false;
    }

    parsed.checkAccess(source);
    return true;
  }

  interface ParsedOption {
    StringRange range();

    String usedLabel();

    Option option();

    void checkAccess(CommandSource source) throws CommandSyntaxException;
  }

  interface ParsedValue<T> extends ParsedOption {
    @Override
    ArgumentOption<T> option();

    T value();
  }
}
package net.forthecrown.grenadier.types.options;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import net.forthecrown.grenadier.CommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class EmptyOptions implements ParsedOptions {

  @Override
  public ParsedOptions checkAccess(CommandSource source) {
    return this;
  }

  @Override
  public @Nullable ParsedOption getParsedOption(@NotNull Option option) {
    return null;
  }

  @Override
  public @Nullable ParsedOption getParsedOption(@NotNull String label) {
    return null;
  }

  @Override
  public @Nullable <T> ParsedValue<T> getParsedValue(ArgumentOption<T> option) {
    return null;
  }

  @Override
  public boolean has(Option option) {
    return false;
  }

  @Override
  public <T> @Nullable T getValue(ArgumentOption<T> option) {
    return option.getDefaultValue();
  }

  @Override
  public <T> Optional<T> getValueOptional(ArgumentOption<T> option) {
    return Optional.ofNullable(option.getDefaultValue());
  }

  @Override
  public <T> T getValue(ArgumentOption<T> option, CommandSource source) {
    return option.getDefaultValue();
  }

  @Override
  public <T> Optional<T> getValueOptional(ArgumentOption<T> option,
                                          CommandSource source
  ) {
    return Optional.ofNullable(option.getDefaultValue());
  }

  @Override
  public boolean hasFlag(FlagOption option, CommandSource source) {
    return false;
  }

  @Override
  public Collection<ParsedOption> parsedOptions() {
    return Collections.emptyList();
  }
}
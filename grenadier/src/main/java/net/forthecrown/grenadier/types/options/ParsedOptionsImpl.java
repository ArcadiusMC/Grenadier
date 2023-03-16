package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ParsedOptionsImpl implements ParsedOptions {

  private final String input;

  private final Map<String, ParsedOption> lookup = new HashMap<>();
  private final Map<Option, ParsedOption> options = new HashMap<>();

  public ParsedOptionsImpl(String input) {
    this.input = input;
  }

  public void addFlag(Option option, String label, StringRange range) {
    ParsedOption parsed = new ParsedOptionImpl(option, label, range);
    add(parsed);
  }

  public <T> void addValue(ArgumentOption<T> option,
                           T value,
                           String label,
                           StringRange range
  ) {
    ParsedValue<T> parsed = new ParsedValueImpl<>(option, label, range, value);
    add(parsed);
  }

  private void add(ParsedOption option) {
    option.option().getLabels().forEach(s -> {
      lookup.put(s, option);
    });

    options.put(option.option(), option);
  }

  @Override
  public @Nullable ParsedOption getParsedOption(@NotNull Option option) {
    return options.get(option);
  }

  @Override
  public @Nullable ParsedOption getParsedOption(@NotNull String label) {
    return lookup.get(label);
  }

  @Override
  public String toString() {
    return options.values().toString();
  }

  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor
  class ParsedOptionImpl implements ParsedOption {

    final Option option;
    final String usedLabel;
    final StringRange range;

    @Override
    public void checkAccess(CommandSource source)
        throws CommandSyntaxException
    {
      if (option.test(source)) {
        return;
      }

      StringReader reader = Readers.create(input, range.getStart());
      throw Grenadier.exceptions().unknownOption(reader, usedLabel);
    }

    @Override
    public String toString() {
      return "ParsedOption{" +
          ", usedLabel='" + usedLabel + '\'' +
          ", range=" + range +
          '}';
    }
  }

  @Getter
  @Accessors(fluent = true)
  class ParsedValueImpl<T> extends ParsedOptionImpl implements ParsedValue<T> {

    private final T value;

    public ParsedValueImpl(ArgumentOption<T> option,
                           String usedLabel,
                           StringRange range,
                           T value
    ) {
      super(option, usedLabel, range);
      this.value = value;
    }

    @Override
    public ArgumentOption<T> option() {
      return (ArgumentOption<T>) super.option();
    }

    @Override
    public String toString() {
      return "ParsedValue{" +
          "value=" + value +
          ", usedLabel='" + usedLabel + '\'' +
          ", range=" + range +
          '}';
    }
  }
}
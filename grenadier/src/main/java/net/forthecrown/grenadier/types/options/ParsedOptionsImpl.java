package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.types.options.OptionsArgumentImpl.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ParsedOptionsImpl implements ParsedOptions {

  private final String input;

  private final Map<String, ParsedOptionImpl> lookup = new HashMap<>();
  private final Map<Option, ParsedOptionImpl> options = new HashMap<>();

  public ParsedOptionsImpl(String input) {
    this.input = input;
  }

  public void addFlag(Entry option, String label, StringRange range) {
    ParsedOptionImpl parsed = new ParsedOptionImpl(option, label, range);
    add(parsed);
  }

  public <T> void addValue(Entry option,
                           T value,
                           String label,
                           StringRange range
  ) {
    ParsedValueImpl<T> parsed = new ParsedValueImpl<>(option, label, range, value);
    add(parsed);
  }

  private void add(ParsedOptionImpl option) {
    lookup.put(option.usedLabel(), option);
    options.put(option.option(), option);
  }

  @Override
  public ParsedOptions checkAccess(CommandSource source) throws CommandSyntaxException {
    for (var parsed: options.values()) {
      parsed.checkAccess(source);
    }

    for (ParsedOptionImpl value : options.values()) {
      var exclusive = value.entry.exclusive();
      var requires = value.entry.requires();

      for (var excl : exclusive) {
        if (!has(excl)) {
          continue;
        }

        String label = value.usedLabel();
        throw Grenadier.exceptions().exclusiveOption(label, exclusive);
      }

      for (var req : requires) {
        if (has(req)) {
          continue;
        }

        throw Grenadier.exceptions().missingRequired(value.usedLabel(), requires);
      }
    }

    return this;
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
  public Collection<ParsedOption> parsedOptions() {
    return Collections.unmodifiableCollection(options.values());
  }

  @Override
  public String toString() {
    return options.values().toString();
  }

  @Getter
  @Accessors(fluent = true)
  @RequiredArgsConstructor
  class ParsedOptionImpl implements ParsedOption {

    final Entry entry;
    final String usedLabel;
    final StringRange range;

    @Override
    public Option option() {
      return entry.option();
    }

    @Override
    public void checkAccess(CommandSource source)
        throws CommandSyntaxException
    {
      if (option().test(source)) {
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

    public ParsedValueImpl(Entry option,
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
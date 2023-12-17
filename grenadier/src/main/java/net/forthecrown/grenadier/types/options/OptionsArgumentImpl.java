package net.forthecrown.grenadier.types.options;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.options.OptionsArgumentBuilder.EntryBuilder;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

class OptionsArgumentImpl implements OptionsArgument, VanillaMappedArgument {

  private static final Logger LOGGER = Grenadier.getLogger();

  final ImmutableMap<String, Entry> optionLookup;
  final ImmutableMap<Option, Entry> entries;
  final ImmutableSet<Entry> options;

  public OptionsArgumentImpl(BuilderImpl builder) {
    this.optionLookup = ImmutableMap.copyOf(builder.options);
    this.entries = ImmutableMap.copyOf(builder.entries);
    this.options = ImmutableSet.copyOf(optionLookup.values());

    Preconditions.checkArgument(!options.isEmpty(), "No options given");

    for (Entry option : options) {
      for (Option excl : option.exclusive) {
        Entry exclEntry = entries.get(excl);

        // Can happen if 'option' was registered,
        // but 'excl' itself was not
        if (exclEntry == null) {
          continue;
        }

        exclEntry.exclusive.add(option.option);
      }
    }
  }

  @Override
  public ParsedOptions parse(StringReader reader)
      throws CommandSyntaxException
  {
    OptionsParser parser = new OptionsParser(reader, this);
    parser.parse();
    return parser.getOptions();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource)) {
      return Suggestions.empty();
    }

    StringReader reader = Readers.forSuggestions(builder);
    OptionsParser parser = new OptionsParser(reader, this);

    try {
      parser.parse();
    } catch (CommandSyntaxException exc) {
      // Ignored
    }

    return parser.getSuggestions(
        (CommandContext<CommandSource>) context,
        builder
    );
  }

  @Override
  public @Nullable ArgumentEntry getEntry(Option option) {
    return entries.get(option);
  }

  @Override
  public Collection<ArgumentEntry> getEntries() {
    return Collections.unmodifiableCollection(entries.values());
  }

  @Override
  public Option getOption(String label) {
    var entry = optionLookup.get(label);

    if (entry == null) {
      return null;
    }

    return entry.option();
  }

  @Override
  public Set<Option> getOptions() {
    return options.stream()
        .map(ArgumentEntry::option)
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.greedyString();
  }

  record Entry(
      Option option,
      boolean required,
      Set<Option> requires,
      Set<Option> exclusive
  ) implements ArgumentEntry {

    @Override
    public Set<Option> requires() {
      return Collections.unmodifiableSet(requires);
    }

    @Override
    public Set<Option> exclusive() {
      return Collections.unmodifiableSet(exclusive);
    }
  }

  static class BuilderImpl implements OptionsArgumentBuilder {

    final Map<String, Entry> options = new HashMap<>();
    final Map<Option, Entry> entries = new HashMap<>();

    @Override
    public OptionsArgumentBuilder addFlag(FlagOption option) {
      return addOptional(option);
    }

    @Override
    public OptionsArgumentBuilder addOptional(Option option, Consumer<EntryBuilder> consumer) {
      Objects.requireNonNull(consumer, "Null consumer");
      return addOption(option, false, consumer);
    }

    @Override
    public OptionsArgumentBuilder addRequired(Option option, Consumer<EntryBuilder> consumer) {
      Objects.requireNonNull(consumer, "Null consumer");
      return addOption(option, true, consumer);
    }

    @Override
    public OptionsArgumentBuilder addOptional(Option option) {
      return addOption(option, false, null);
    }

    @Override
    public OptionsArgumentBuilder addRequired(Option option) {
      return addOption(option, true, null);
    }

    private OptionsArgumentBuilder addOption(
        Option option,
        boolean required,
        Consumer<EntryBuilder> consumer
    ) {
      EntryBuilderImpl builder = new EntryBuilderImpl(required, option);

      if (consumer != null) {
        consumer.accept(builder);
      }

      return addOption(builder.build());
    }

    private OptionsArgumentBuilder addOption(Entry entry) {
      var option = entry.option;

      var label = option.getLabel();
      var existing = options.get(label);

      Preconditions.checkArgument(
          existing == null,
          "Conflicting option labels! "
              + "'%s' is already in use by another option",
          label
      );

      options.put(label, entry);
      entries.put(option, entry);

      return this;
    }

    @Override
    public OptionsArgument build() {
      return new OptionsArgumentImpl(this);
    }
  }

  static class EntryBuilderImpl implements EntryBuilder {

    private final boolean required;
    private final Option option;

    private final Set<Option> requires = new HashSet<>();
    private final Set<Option> exclusive = new HashSet<>();

    public EntryBuilderImpl(boolean required, Option option) {
      this.required = required;
      this.option = option;
    }

    @Override
    public Option option() {
      return option;
    }

    @Override
    public EntryBuilder requires(Option... options) {
      Objects.requireNonNull(options, "Null option");
      Collections.addAll(requires, options);
      requires.remove(option);
      return this;
    }

    @Override
    public EntryBuilder exclusiveWith(Option... options) {
      Objects.requireNonNull(options, "Null option");
      Collections.addAll(exclusive, options);
      exclusive.remove(option);
      return this;
    }

    public Entry build() {
      return new Entry(option, required, requires, exclusive);
    }
  }
}
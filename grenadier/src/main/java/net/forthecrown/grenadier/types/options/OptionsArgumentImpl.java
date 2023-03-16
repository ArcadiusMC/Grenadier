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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.Nullable;

class OptionsArgumentImpl implements OptionsArgument, VanillaMappedArgument {

  final ImmutableMap<String, Option> optionLookup;
  final ImmutableMap<ArgumentOption<?>, ArgumentEntry<?>> entries;
  final ImmutableSet<Option> options;

  public OptionsArgumentImpl(BuilderImpl builder) {
    this.optionLookup = ImmutableMap.copyOf(builder.options);
    this.entries = ImmutableMap.copyOf(builder.entries);
    this.options = ImmutableSet.copyOf(optionLookup.values());

    Preconditions.checkArgument(options.size() > 1, "No options given");
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
  public @Nullable <T> ArgumentEntry<T> getEntry(ArgumentOption<T> option) {
    return (ArgumentEntry<T>) entries.get(option);
  }

  @Override
  public Collection<ArgumentEntry<?>> getEntries() {
    return Collections.unmodifiableCollection(entries.values());
  }

  @Override
  public Option getOption(String label) {
    return optionLookup.get(label);
  }

  @Override
  public Set<Option> getOptions() {
    return options;
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.greedyString();
  }

  record Entry<T>(ArgumentOption<T> option, boolean required)
      implements ArgumentEntry<T>
  {

  }

  static class BuilderImpl implements Builder {

    final Map<String, Option> options = new HashMap<>();
    final Map<ArgumentOption<?>, ArgumentEntry<?>> entries = new HashMap<>();

    @Override
    public Builder addFlag(FlagOption option) {
      return addOption(option);
    }

    @Override
    public <T> Builder addOption(ArgumentOption<T> option, boolean required) {
      addOption(option);

      Entry<T> entry = new Entry<>(option, required);
      entries.put(option, entry);
      return this;
    }

    private Builder addOption(Option option) {
      option.getLabels().forEach(s -> {
        var existing = options.get(s);

        Preconditions.checkArgument(
            existing == null,
            "Conflicting option labels! "
                + "'%s' is already in use by another option",
            s
        );

        options.put(s, option);
      });

      return this;
    }

    @Override
    public OptionsArgument build() {
      return new OptionsArgumentImpl(this);
    }
  }
}
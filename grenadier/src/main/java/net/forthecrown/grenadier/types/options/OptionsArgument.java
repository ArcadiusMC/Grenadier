package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.types.options.OptionsArgumentImpl.BuilderImpl;
import org.jetbrains.annotations.Nullable;

public interface OptionsArgument extends ArgumentType<ParsedOptions> {

  static Builder builder() {
    return new BuilderImpl();
  }

  @Override
  ParsedOptions parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  @Nullable
  <T> ArgumentEntry<T> getEntry(ArgumentOption<T> option);

  Collection<ArgumentEntry<?>> getEntries();

  Set<Option> getOptions();

  Option getOption(String label);

  interface ArgumentEntry<T> {
    ArgumentOption<T> option();

    boolean required();
  }

  interface Builder {

    Builder addFlag(FlagOption option);

    <T> Builder addOption(ArgumentOption<T> option, boolean required);

    default <T> Builder addOptional(ArgumentOption<T> option) {
      return addOption(option, false);
    }

    default <T> Builder addRequired(ArgumentOption<T> option) {
      return addOption(option, true);
    }

    OptionsArgument build();
  }
}
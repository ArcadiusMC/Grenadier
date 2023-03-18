package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ArgumentTypeMapper {

  public static <F, T> ArgumentType<T> mapType(ArgumentType<F> from,
                                        Function<F, T> translator
  ) {
    if (from instanceof VanillaMappedArgument) {
      return new MappedArgumentTypeVanilla<>(from, translator);
    }

    return new SimpleMappedArgumentType<>(from, translator);
  }

  static class MappedArgumentTypeVanilla<F, T>
      extends SimpleMappedArgumentType<F, T>
      implements VanillaMappedArgument
  {

    public MappedArgumentTypeVanilla(ArgumentType<F> baseType,
                                     Function<F, T> translator
    ) {
      super(baseType, translator);
      assert baseType instanceof VanillaMappedArgument;
    }

    @Override
    public ArgumentType<?> getVanillaType(CommandBuildContext context) {
      return ((VanillaMappedArgument) baseType).getVanillaType(context);
    }

    @Override
    public boolean useVanillaSuggestions() {
      return ((VanillaMappedArgument) baseType).useVanillaSuggestions();
    }
  }

  @RequiredArgsConstructor
  static class SimpleMappedArgumentType<F, T> implements ArgumentType<T> {

    final ArgumentType<F> baseType;
    final Function<F, T> translator;

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
      F parsed = baseType.parse(reader);
      var result = translator.apply(parsed);

      Objects.requireNonNull(
          result,
          "Null result returned by argument type translator"
      );

      return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
        CommandContext<S> context,
        SuggestionsBuilder builder
    ) {
      return baseType.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
      return baseType.getExamples();
    }
  }
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.ArgumentTypes.ArgumentTypeTranslator;
import net.minecraft.commands.CommandBuildContext;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
class ArgumentTypeMapper {

  public static <F, T> ArgumentType<T> mapType(
      ArgumentType<F> from,
      ArgumentTypeTranslator<F, T> translator
  ) {
    if (from instanceof VanillaMappedArgument) {
      return new MappedArgumentTypeVanilla<>(from, translator);
    }

    if (from instanceof SimpleVanillaMapped) {
      return new MappedArgumentTypeSimpleVanilla<>(from, translator);
    }

    return new SimpleMappedArgumentType<>(from, translator);
  }

  static class MappedArgumentTypeSimpleVanilla<F, T>
      extends SimpleMappedArgumentType<F, T>
      implements SimpleVanillaMapped
  {

    public MappedArgumentTypeSimpleVanilla(
        ArgumentType<F> baseType,
        ArgumentTypeTranslator<F, T> translator
    ) {
      super(baseType, translator);
      assert baseType instanceof SimpleVanillaMapped;
    }

    @Override
    public ArgumentType<?> getVanillaType() {
      return ((SimpleVanillaMapped) baseType).getVanillaType();
    }

    @Override
    public boolean useVanillaSuggestions() {
      return ((SimpleVanillaMapped) baseType).useVanillaSuggestions();
    }
  }

  static class MappedArgumentTypeVanilla<F, T>
      extends SimpleMappedArgumentType<F, T>
      implements VanillaMappedArgument
  {

    public MappedArgumentTypeVanilla(
        ArgumentType<F> baseType,
        ArgumentTypeTranslator<F, T> translator
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
    final ArgumentTypeTranslator<F, T> translator;

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
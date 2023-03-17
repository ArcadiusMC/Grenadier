package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;

@Getter
class EnumArgumentImpl<E extends Enum<E>>
    implements EnumArgument<E>, VanillaMappedArgument
{

  private final Class<E> enumType;
  private final Map<String, E> lookupMap;

  public EnumArgumentImpl(Class<E> enumType) {
    this.enumType = enumType;

    this.lookupMap = Arrays.stream(enumType.getEnumConstants())
        .collect(Collectors.toMap(e -> e.name().toLowerCase(), e -> e));
  }

  @Override
  public E parse(StringReader reader) throws CommandSyntaxException {
    final int cursor = reader.getCursor();

    String word = reader.readUnquotedString();
    E value = lookupMap.get(word.toLowerCase());

    if (value == null) {
      reader.setCursor(cursor);
      throw Grenadier.exceptions().invalidEnum(enumType, word, reader);
    }

    return value;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder, lookupMap.keySet());
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.word();
  }
}
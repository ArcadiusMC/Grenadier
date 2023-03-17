package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

/**
 * Parses an enum value by its name.
 *
 * <p>
 * Example for {@link net.kyori.adventure.text.format.TextDecoration}: <pre><code>
 * static final EnumArgument&lt;TextDecoration> ARGUMENT
 *     = ArgumentTypes.enumType(TextDecoration.class);
 * </code></pre>
 * Let's assume the input we're given is {@code italic}, we can then use it
 * like so: <pre><code>
 * CommandContext&lt;CommandSource> context = // ...
 * TextDecoration decoration
 *     = context.getArgument("argument name", TextDecoration.class);
 *
 * assert decoration == TextDecoration.ITALIC;
 * </code></pre>
 *
 * @param <E> Enum type
 */
public interface EnumArgument<E extends Enum<E>> extends ArgumentType<E> {

  @Override
  E parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  /**
   * Gets the enum class
   * @return Enum class
   */
  Class<E> getEnumType();
}
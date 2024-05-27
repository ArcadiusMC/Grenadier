package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.forthecrown.grenadier.types.ItemFilterArgument.Result;
import org.bukkit.inventory.ItemStack;

/**
 * Parses a similar result to {@link ItemArgument}, with the difference that
 * this argument type allows item tags, allowing this to match several materials
 * at once.
 * <p>
 * Input examples: <pre>
 * minecraft:stone
 * minecraft:stone{tagKey:1b}
 * #minecraft:buttons
 * #minecraft:buttons{tagKey:0b}
 * </pre>
 */
public interface ItemFilterArgument extends ArgumentType<Result> {

  @Override
  Result parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  /**
   * Parsed predicate result
   */
  interface Result extends Predicate<ItemStack> {

    /**
     * Tests an item against this predicate.
     *
     * @param itemStack the input argument
     * @return {@code true} if the item matches, {@code false} otherwise
     */
    @Override
    boolean test(ItemStack itemStack);
  }
}
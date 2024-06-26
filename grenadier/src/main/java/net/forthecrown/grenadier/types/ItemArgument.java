package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.types.ItemArgument.Result;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Argument type that parses item data like an the material the item is made of
 * and NBT data.
 * <p>
 * Input examples: <pre>
 * minecraft:stone
 * minecraft:stone[custom_data={tagKey:1b}]
 * </pre>
 */
public interface ItemArgument extends ArgumentType<Result> {

  @Override
  Result parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  /**
   * Parsed item result, contains the data that was parsed which can be used to
   * create an itemstack instance
   */
  interface Result {

    /**
     * Gets the Material of the item that was parsed
     * @return Parsed material
     */
    Material getMaterial();

    /**
     * Creates a new {@link ItemStack} from this parsed data
     *
     * @param amount         Item quantity
     * @param validateAmount {@code true} to ensure the {@code amount} doesn't
     *                       exceed the max stack size of the
     *                       {@link #getMaterial()}
     *
     * @return Created item stack
     *
     * @throws CommandSyntaxException If {@code validateAmount == true} and the
     *                                {@code amount} parameter exceeded the max
     *                                stack size
     */
    ItemStack create(int amount, boolean validateAmount)
        throws CommandSyntaxException;
  }
}
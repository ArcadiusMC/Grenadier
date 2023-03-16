package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.forthecrown.grenadier.types.ItemFilterArgument.Result;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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
     * Gets an unmodifiable list of all materials that are valid for this
     * result
     *
     * @return Valid materials
     */
    Set<Material> getMaterials();

    /**
     * Gets a copy of the parsed NBT tag
     * @return Parsed NBT tag or {@code null}, if no NBT was given in the input
     */
    @Nullable
    CompoundTag getTag();

    /**
     * Tests an item against this predicate.
     * <p>
     * For a true result to be returned, the item's type must match the
     * {@link #getMaterials()} set and, if any NBT data was set, then the NBT
     * data of the specified item must also match {@link #getTag()}
     *
     * @param itemStack the input argument
     * @return {@code true} if the item matches, {@code false} otherwise
     */
    @Override
    boolean test(ItemStack itemStack);
  }
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.forthecrown.grenadier.types.BlockFilterArgument.Result;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parses either a block or a block tag, along with some block state data and
 * an optional NBT Tag. The result can be used to test blocks, block states and
 * block data instances.
 * <p>
 * Input examples: <pre>
 * minecraft:stone
 * minecraft:stone_stairs[facing=west]
 * minecraft:chest[facing=west]{Items:[]}
 * #minecraft:stairs
 * #minecraft:stairs[facing=west]
 * </pre>
 */
public interface BlockFilterArgument extends ArgumentType<Result> {

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
  interface Result extends Predicate<Block> {

    /**
     * Gets all materials that are valid for this result
     * @return Valid materials
     */
    @NotNull
    Set<Material> getMaterials();

    /**
     * Gets the parsed NBT tag
     * @return Parsed NBT, or {@code null}, if not NBT was specified
     */
    @Nullable
    CompoundTag getTag();

    /**
     * Gets a name to value map of all explicitly parsed properties
     * @return Property map
     */
    @NotNull
    Map<String, String> getParsedProperties();

    /**
     * Calls {@link #test(BlockState)} with {@link Block#getState()}
     * @param block the input argument
     * @return {@link #test(BlockState)}
     */
    @Override
    default boolean test(Block block) {
      return test(block.getState());
    }

    /**
     * Tests the specified {@code state} against this result
     * <p>
     * First, this calls {@link #test(BlockData)} with
     * {@link BlockState#getBlockData()}, if that returns {@code false}, then so
     * does this method.
     * <p>
     * Next, if {@link #getTag()} is {@code null}, then this method returns
     * {@code true}. Otherwise, it checks if the input is a tile entity, if it
     * isn't, returns {@code false}. Finally, the NBT of this result and the NBT
     * of the tile entity are compared with
     * {@link net.forthecrown.nbt.BinaryTags#compareTags(BinaryTag, BinaryTag, boolean)}
     *
     * @param state State to test
     * @return {@code true}, if the input matched this result, {@code false}
     *         otherwise
     */
    boolean test(BlockState state);

    /**
     * Tests the specified {@code data} against this result.
     * <p>
     * First, this method ensures that the input's type is contained in
     * {@link #getMaterials()}. Then it compares each property in
     * {@link #getParsedProperties()}. If a property cannot be found in the
     * input or doesn't have a matching value for that property, this method
     * fails.
     * <p>
     * If the data's type and properties match, this method returns {@code true}
     *
     * @param data Data to test
     * @return {@code true}, if the input matched this result, {@code false}
     *         otherwise
     */
    boolean test(BlockData data);
  }
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.types.BlockArgument.Result;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Parses a {@link BlockData} instance along with optional NBT data
 * <p>
 * Input examples: <pre>
 * minecraft:stone
 * minecraft:stone_brick_slab[type=bottom]
 * minecraft:chest{Items:[]}
 * minecraft:chest[facing=west]{Items:[]}
 * </pre>
 */
public interface BlockArgument extends ArgumentType<Result> {

  @Override
  Result parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  /**
   * Parsed block state result
   */
  interface Result {

    /**
     * Gets a name to value map of all block properties that were explicitly
     * parsed.
     *
     * @return Parsed properties
     */
    @NotNull
    Map<String, String> getParsedProperties();

    /**
     * Gets the NBT tag that was parsed
     * @return Parsed NBT, or {@code null}, if no NBT was given in the input
     */
    @Nullable
    CompoundTag getTag();

    /**
     * Gets the parsed block state
     * @return Parsed block data
     */
    @NotNull
    BlockData getParsedState();

    /**
     * Places the parsed block in the specified {@code world} at the
     * {@code x, y, z} coordinates and optionally sets any NBT that was parsed,
     * if, the parsed block type is a tile entity.
     *
     * @param world World to place the block in
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param updatePhysics {@code true} to update the placed block and any
     *                      surrounding blocks, {@code false} otherwise
     */
    void place(World world, int x, int y, int z, boolean updatePhysics);

    /**
     * Delegate for {@link #place(World, int, int, int, boolean)} with the
     * 'updatePhysics' parameter set to {@code true}
     * @see #place(World, int, int, int, boolean)
     */
    default void place(World world, int x, int y, int z) {
      place(world, x, y, z, true);
    }

    /**
     * Delegate for {@link #place(World, int, int, int, boolean)} where the
     * world and coordinates are gotten from the specified {@code location}
     *
     * @param location Location to place the block at
     * @param updatePhysics {@code true} to update the placed block and any
     *                      surrounding blocks, {@code false} otherwise
     */
    default void place(Location location, boolean updatePhysics) {
      place(
          location.getWorld(),
          location.getBlockX(),
          location.getBlockY(),
          location.getBlockZ(),
          updatePhysics
      );
    }

    /**
     * Delegate for {@link #place(World, int, int, int, boolean)} where the
     * world and coordinates are gotten from the specified {@code location} and
     * where the 'updatePhysics' parameter is set to {@code true}
     *
     * @see #place(World, int, int, int, boolean)
     */
    default void place(Location location) {
      place(
          location.getWorld(),
          location.getBlockX(),
          location.getBlockY(),
          location.getBlockZ(),
          true
      );
    }
  }
}
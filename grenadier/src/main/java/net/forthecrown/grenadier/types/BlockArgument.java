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

  interface Result {

    @NotNull
    Map<String, String> getParsedProperties();

    @Nullable
    CompoundTag getTag();

    @NotNull
    BlockData getParsedState();

    void place(World world, int x, int y, int z, boolean updatePhysics);

    default void place(World world, int x, int y, int z) {
      place(world, x, y, z, true);
    }

    default void place(Location location, boolean updatePhysics) {
      place(
          location.getWorld(),
          location.getBlockX(),
          location.getBlockY(),
          location.getBlockZ(),
          updatePhysics
      );
    }

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
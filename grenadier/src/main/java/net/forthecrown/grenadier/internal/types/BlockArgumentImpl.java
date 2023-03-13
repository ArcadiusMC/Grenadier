package net.forthecrown.grenadier.internal.types;

import static net.forthecrown.grenadier.internal.types.BlockFilterArgumentImpl.lookup;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.forthecrown.grenadier.types.BlockArgument;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.nbt.paper.TagTranslators;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.World;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public class BlockArgumentImpl implements BlockArgument {

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    BlockResult result = BlockStateParser.parseForBlock(lookup(), reader, true);
    return new ResultImpl(result);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return BlockStateParser.fillSuggestions(lookup(), builder, false, true);
  }

  static class ResultImpl implements Result {

    private final CompoundTag tag;
    private final BlockData data;
    private final Set<String> propertyNames;

    public ResultImpl(BlockResult result) {
      this.tag = result.nbt() == null
          ? null
          : TagTranslators.COMPOUND.toApiType(result.nbt());

      this.data = result.blockState().createCraftBlockData();

      this.propertyNames = result.properties().keySet()
          .stream()
          .map(Property::getName)
          .collect(Collectors.toSet());
    }

    @Override
    public @NotNull Set<String> getParsedProperties() {
      return Collections.unmodifiableSet(propertyNames);
    }

    public CompoundTag getTag() {
      return tag == null ? null : tag.copy();
    }

    @Override
    public @NotNull BlockData getParsedState() {
      return data.clone();
    }

    @Override
    public void place(World world, int x, int y, int z, boolean updatePhysics) {
      var block = world.getBlockAt(x, y, z);
      block.setBlockData(getParsedState(), updatePhysics);

      if (tag == null) {
        return;
      }

      var state = block.getState();

      if (state instanceof TileState tile) {
        PaperNbt.loadBlockEntity(tile, tag);
      }
    }
  }
}
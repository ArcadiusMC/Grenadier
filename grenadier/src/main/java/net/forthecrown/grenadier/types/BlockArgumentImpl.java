package net.forthecrown.grenadier.types;

import static net.forthecrown.grenadier.types.BlockFilterArgumentImpl.lookup;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.nbt.paper.TagTranslators;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.World;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

class BlockArgumentImpl implements BlockArgument, VanillaMappedArgument {

  static final BlockArgument INSTANCE = new BlockArgumentImpl();

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

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return BlockStateArgument.block(context);
  }

  static class ResultImpl implements Result {

    private final CompoundTag tag;
    private final BlockData data;
    private final Map<String, String> properties;

    public ResultImpl(BlockResult result) {
      this.tag = result.nbt() == null
          ? null
          : TagTranslators.COMPOUND.toApiType(result.nbt());

      this.data = result.blockState().createCraftBlockData();

      this.properties = result.properties().entrySet()
          .stream()
          .map(entry -> {
            Property key = entry.getKey();


            String name = key.getName();
            String value = key.getName(entry.getValue());

            return Map.entry(name, value);
          })

          .collect(InternalUtil.mapCollector());
    }

    @Override
    public @NotNull Map<String, String> getParsedProperties() {
      return Collections.unmodifiableMap(properties);
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

    @Override
    public String toString() {
      return "Result{" +
          "tag=" + tag +
          ", state=" + data +
          '}';
    }
  }
}
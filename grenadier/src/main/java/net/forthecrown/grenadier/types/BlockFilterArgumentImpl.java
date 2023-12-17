package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult;
import net.minecraft.commands.arguments.blocks.BlockStateParser.TagResult;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class BlockFilterArgumentImpl
    implements BlockFilterArgument, VanillaMappedArgument
{

  static final BlockFilterArgument INSTANCE = new BlockFilterArgumentImpl();

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    return BlockStateParser.parseForTesting(lookup(), reader, true)
        .map(ResultImpl::new, ResultImpl::new);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return BlockStateParser.fillSuggestions(lookup(), builder, true, true);
  }

  static HolderLookup<Block> lookup() {
    return InternalUtil.lookup(Registries.BLOCK);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return BlockPredicateArgument.blockPredicate(context);
  }

  static class ResultImpl implements Result {

    private final Set<Material> materials;
    private final Map<String, String> properties;

    private final CompoundTag tag;

    ResultImpl(BlockResult result) {
      this.tag = InternalUtil.fromVanillaTag(result.nbt());

      this.materials = new HashSet<>();
      materials.add(
          CraftMagicNumbers.getMaterial(result.blockState().getBlock())
      );

      this.properties = result.properties()
          .entrySet()
          .stream()
          .map(entry -> {
            Property prop = entry.getKey();

            return Map.entry(
                prop.getName(),
                prop.getName(entry.getValue())
            );
          })

          .collect(InternalUtil.mapCollector());
    }

    ResultImpl(TagResult result) {
      this.tag = InternalUtil.fromVanillaTag(result.nbt());

      this.materials = result.tag().stream()
          .map(Holder::value)
          .map(CraftMagicNumbers::getMaterial)
          .collect(Collectors.toSet());

      this.properties = new HashMap<>(result.vagueProperties());
    }

    @Override
    public @NotNull Set<Material> getMaterials() {
      return Collections.unmodifiableSet(materials);
    }

    @Override
    public @Nullable CompoundTag getTag() {
      return tag == null ? null : tag.copy();
    }

    @Override
    public @NotNull Map<String, String> getParsedProperties() {
      return Collections.unmodifiableMap(properties);
    }

    private boolean testMaterial(Material material) {
      return materials.contains(material);
    }

    @Override
    public boolean test(BlockState state) {
      var data = state.getBlockData();

      if (!test(data)) {
        return false;
      }

      if (tag == null) {
        return true;
      }

      if (!(state instanceof TileState s)) {
        return false;
      }

      CompoundTag saved = PaperNbt.saveBlockEntity(s);
      return BinaryTags.compareTags(tag, saved, true);
    }

    @Override
    public boolean test(BlockData data) {
      if (!testMaterial(data.getMaterial())) {
        return false;
      }

      CraftBlockData craftData = (CraftBlockData) data;
      var state = craftData.getState();
      var stateDefinition = state.getBlock().getStateDefinition();

      for (var e: properties.entrySet()) {
        Property<?> prop = stateDefinition.getProperty(e.getKey());

        if (prop == null) {
          return false;
        }

        var optional = prop.getValue(e.getValue());

        if (optional.isEmpty()) {
          return false;
        }

        Comparable value = optional.get();

        if (!Objects.equals(value, state.getValue(prop))) {
          return false;
        }
      }

      return true;
    }

    @Override
    public String toString() {
      return "Result{" +
          "materials=" + materials +
          ", properties=" + properties +
          ", tag=" + tag +
          '}';
    }
  }
}
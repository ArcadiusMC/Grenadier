package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.paper.PaperNbt;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.arguments.item.ItemParser.ItemResult;
import net.minecraft.commands.arguments.item.ItemParser.TagResult;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;

class ItemFilterArgumentImpl
    extends AbstractItemArgument
    implements ItemFilterArgument
{

  static final ItemFilterArgument INSTANCE = new ItemFilterArgumentImpl();

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    Either<ItemResult, TagResult> either 
        = ItemParser.parseForTesting(holderLookup, reader);

    return either.map(itemResult -> {
      var set = HolderSet.direct(itemResult.item());
      return new ResultImpl(set, itemResult.nbt());
    }, tagResult -> {
      return new ResultImpl(tagResult.tag(), tagResult.nbt());
    });
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context, SuggestionsBuilder builder
  ) {
    return ItemParser.fillSuggestions(holderLookup, builder, true);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return ItemPredicateArgument.itemPredicate(context);
  }

  public static class ResultImpl extends AbstractItemResult implements Result {
    private final Set<Material> materials;

    public ResultImpl(HolderSet<Item> holders,
                      net.minecraft.nbt.CompoundTag tag
    ) {
      super(tag);
      this.materials = holders.stream()
          .map(holder -> CraftMagicNumbers.getMaterial(holder.value()))
          .collect(Collectors.toSet());
    }

    @Override
    public Set<Material> getMaterials() {
      return Collections.unmodifiableSet(materials);
    }

    @Override
    public boolean test(ItemStack itemStack) {
      if (!materials.contains(itemStack.getType())) {
        return false;
      }

      BinaryTag itemTag = PaperNbt.saveItem(itemStack).get("tag");
      return BinaryTags.compareTags(this.tag, itemTag, true);
    }

    @Override
    public String toString() {
      return "Result{" +
          "materials=" + materials +
          ", tag=" + tag +
          '}';
    }
  }
}
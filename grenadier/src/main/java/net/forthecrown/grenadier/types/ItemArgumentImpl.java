package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemParser;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;

class ItemArgumentImpl
    extends AbstractItemArgument
    implements ItemArgument
{

  static final ItemArgument INSTANCE = new ItemArgumentImpl();

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    ItemParser.ItemResult result = ItemParser.parseForItem(holderLookup, reader);

    return new ItemResult(result);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.item.ItemArgument.item(context);
  }

  @Getter
  public static class ItemResult extends AbstractItemResult implements Result {
    private final Material material;

    public ItemResult(ItemParser.ItemResult result) {
      super(result.nbt());
      this.material = CraftMagicNumbers.getMaterial(result.item().value());
    }

    @Override
    public ItemStack create(int amount, boolean validateAmount)
        throws CommandSyntaxException
    {
      var material = getMaterial();
      int max = material.getMaxStackSize();

      if (validateAmount && amount > max) {
        throw Grenadier.exceptions().overstacked(material);
      }

      CompoundTag itemTag = BinaryTags.compoundTag();
      itemTag.putString("id", material.getKey().asString());
      itemTag.putInt("Count", amount);

      if (tag != null) {
        itemTag.put("tag", tag.copy());
      }

      return PaperNbt.loadItem(itemTag);
    }
  }
}
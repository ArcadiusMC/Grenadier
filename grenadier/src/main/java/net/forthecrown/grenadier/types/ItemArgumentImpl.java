package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;

class ItemArgumentImpl
    extends AbstractItemArgument
    implements ItemArgument
{

  static final ItemArgument INSTANCE = new ItemArgumentImpl();

  private final ItemParser parser;

  public ItemArgumentImpl() {
    this.parser = new ItemParser(InternalUtil.CONTEXT);
  }

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    ItemParser.ItemResult result = parser.parse(reader);
    return new ItemResult(result);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return parser.fillSuggestions(builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.item.ItemArgument.item(context);
  }

  @Getter
  public static class ItemResult implements Result {

    private final ItemInput input;
    private final ItemParser.ItemResult result;
    private final Material material;

    public ItemResult(ItemParser.ItemResult result) {
      this.result = result;
      this.input = new ItemInput(result.item(), result.components(), result.patch());
      this.material = CraftMagicNumbers.getMaterial(result.item().value());
    }

    @Override
    public ItemStack create(int amount, boolean validateAmount)
        throws CommandSyntaxException
    {
      net.minecraft.world.item.ItemStack nmsItem = input.createItemStack(amount, validateAmount);
      return CraftItemStack.asCraftMirror(nmsItem);
    }

    @Override
    public String toString() {
      String itemName = material.key().toString();
      return itemName + input.serialize(InternalUtil.CONTEXT);
    }
  }
}
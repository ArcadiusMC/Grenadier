package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

class ItemFilterArgumentImpl
    extends AbstractItemArgument
    implements ItemFilterArgument
{

  static final ItemFilterArgument INSTANCE = new ItemFilterArgumentImpl();

  private final ItemPredicateArgument argument;

  public ItemFilterArgumentImpl() {
    this.argument = new ItemPredicateArgument(InternalUtil.CONTEXT);
  }

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    return new ResultImpl(argument.parse(reader));
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context, SuggestionsBuilder builder
  ) {
    return argument.listSuggestions(context, builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return ItemPredicateArgument.itemPredicate(context);
  }

  public static class ResultImpl implements Result {
    final ItemPredicateArgument.Result result;

    public ResultImpl(ItemPredicateArgument.Result result) {
      this.result = result;
    }

    @Override
    public boolean test(ItemStack itemStack) {
      var nms = CraftItemStack.asNMSCopy(itemStack);
      return result.test(nms);
    }

    @Override
    public String toString() {
      return "Result{}";
    }
  }
}
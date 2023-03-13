package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public abstract class AbstractItemArgument implements VanillaMappedArgument {
  protected final HolderLookup<Item> holderLookup
      = InternalUtil.lookup(Registries.ITEM);

  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return ItemParser.fillSuggestions(holderLookup, builder, true);
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}
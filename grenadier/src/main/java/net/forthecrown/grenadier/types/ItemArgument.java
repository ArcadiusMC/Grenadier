package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.types.ItemArgument.Result;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemArgument extends ArgumentType<Result> {

  @Override
  Result parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  interface Result {
    Material getMaterial();

    @Nullable
    CompoundTag getTag();

    ItemStack create(int amount, boolean validateAmount)
        throws CommandSyntaxException;

    default ItemStack singular() throws CommandSyntaxException {
      return create(1, true);
    }
  }
}
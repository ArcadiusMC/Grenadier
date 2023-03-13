package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.forthecrown.grenadier.types.BlockFilterArgument.Result;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockFilterArgument extends ArgumentType<Result> {

  @Override
  Result parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );

  interface Result extends Predicate<Block> {

    @NotNull
    Set<Material> getMaterials();

    @Nullable
    CompoundTag getTag();

    @NotNull
    Set<String> getParsedProperties();

    @Override
    default boolean test(Block block) {
      return test(block.getState());
    }

    boolean test(BlockState state);

    boolean test(BlockData data);
  }
}
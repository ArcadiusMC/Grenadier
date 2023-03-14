package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

class LootTableArgumentImpl
    implements LootTableArgument, VanillaMappedArgument
{

  static final LootTableArgument INSTANCE = new LootTableArgumentImpl();

  @Override
  public LootTable parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();

    NamespacedKey key = ArgumentTypes.key().parse(reader);
    LootTable lootTable = Bukkit.getLootTable(key);

    if (lootTable == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownLootTable(key, reader);
    }

    return lootTable;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    MinecraftServer server = MinecraftServer.getServer();
    var idSet = server.getLootTables().getIds();

    return SharedSuggestionProvider.suggestResource(idSet, builder);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return ResourceLocationArgument.id();
  }
}
package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.SyntaxExceptions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.TagTranslators;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.flag.FeatureFlags;

public class InternalUtil {
  public static final CommandBuildContext CONTEXT
      = CommandBuildContext.simple(
      DedicatedServer.getServer().registryAccess(),
      FeatureFlags.DEFAULT_FLAGS
  );

  public static final SuggestionProvider<CommandSource> SUGGEST_ALL_COMMANDS = (context, builder) -> {
    StringReader reader = Readers.forSuggestions(builder);
    CommandSourceStack stack = unwrap(context.getSource());

    CommandDispatcher<CommandSourceStack> dispatcher
        = DedicatedServer.getServer().getCommands().getDispatcher();

    ParseResults<CommandSourceStack> parseResults
        = dispatcher.parse(reader, stack);

    return dispatcher.getCompletionSuggestions(parseResults);
  };

  public static <T> RegistryLookup<T> lookup(ResourceKey<? extends Registry<T>> reg) {
    return DedicatedServer.getServer()
        .registryAccess()
        .lookupOrThrow(reg);
  }

  public static CommandSource wrap(CommandSourceStack stack) {
    return new CommandSourceImpl(stack);
  }

  public static CommandSourceStack unwrap(CommandSource source) {
    return ((CommandSourceImpl) source).getStack();
  }

  public static StringReader ofBukkit(String label, String[] args) {
    StringJoiner joiner = new StringJoiner(" ", label, "");
    for (String arg : args) {
      joiner.add(arg);
    }
    return new StringReader(joiner.toString());
  }

  public static <K, V> Collector<Entry<K, V>, ?, Map<K, V>> mapCollector() {
    return Collectors.toMap(Entry::getKey, Entry::getValue);
  }

  public static CompoundTag fromVanillaTag(net.minecraft.nbt.CompoundTag tag) {
    return tag == null ? null : TagTranslators.COMPOUND.toApiType(tag);
  }

  public static int execute(CommandSource source, StringReader reader) {
    final StringReader startReader = Readers.copy(reader);

    try {
      return Grenadier.dispatcher().execute(reader, source);
    } catch (CommandSyntaxException exc) {
      SyntaxExceptions.handle(exc, source);
      return 1;
    } catch (Throwable t) {
      Grenadier.exceptionHandler()
          .onCommandException(startReader, t, source);

      return 1;
    }
  }
}
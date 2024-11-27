package net.forthecrown.grenadier.internal;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.SyntaxExceptions;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.TagTranslators;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.flag.FeatureFlags;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;

@Internal
public final class InternalUtil {
  private InternalUtil() {}

  private static final Logger LOGGER = Grenadier.getLogger();

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

  public static StringReader bukkitReader(String label, String[] args) {
    List<String> arguments = Lists.asList(label, args);
    return new StringReader(Joiner.on(' ').join(arguments));
  }

  public static <K, V> Collector<Entry<K, V>, ?, Map<K, V>> mapCollector() {
    return Collectors.toMap(Entry::getKey, Entry::getValue);
  }

  public static CompoundTag fromVanillaTag(net.minecraft.nbt.CompoundTag tag) {
    return tag == null ? null : TagTranslators.COMPOUND.toApiType(tag);
  }

  public static Predicate<String> createPredicate(ReaderPredicate predicate) {
    return s -> {
      StringReader reader = new StringReader(s);

      try {
        return predicate.parse(reader);
      } catch (CommandSyntaxException exc) {
        return false;
      }
    };
  }

  public static int execute(CommandSource source, StringReader reader) {
    final StringReader startReader = Readers.copy(reader);

    CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = true;

    CommandDispatcher<CommandSource> dispatcher = Grenadier.dispatcher();
    ParseResults<CommandSource> results = dispatcher.parse(reader, source);

    if (LOGGER.isDebugEnabled() && false) {
      printDebugInfo(dispatcher, results, reader);
    }

    try {
      return dispatcher.execute(results);
    } catch (CommandSyntaxException exc) {
      SyntaxExceptions.handle(exc, source);
      return 0;
    } catch (Throwable t) {
      Grenadier.getProvider()
          .getExceptionHandler()
          .onCommandException(startReader, t, source);

      return 0;
    }
  }

  private static void printDebugInfo(
      CommandDispatcher<CommandSource> dispatcher,
      ParseResults<CommandSource> results,
      StringReader reader
  ) {
    LOGGER.debug("exceptions:");
    results.getExceptions().forEach((node, exc) -> {
      LOGGER.debug("  - node=\"{}\" exception:", dispatcher.getPath(node), exc);
    });

    CommandContextBuilder<CommandSource> ctx = results.getContext().getLastChild();

    LOGGER.debug("ctx:");
    LOGGER.debug("  range={}", ctx.getRange());
    LOGGER.debug("  input={}", get(ctx.getRange(), reader));

    LOGGER.debug("arguments:");
    ctx.getArguments().forEach((s, arg) -> {
      StringRange range = arg.getRange();
      String inp = get(range, reader);

      LOGGER.debug("  - {}: range={} input=\"{}\" result={}", s, range, inp, arg.getResult());
    });

    LOGGER.debug("nodes:");
    ctx.getNodes().forEach(node -> {
      StringRange range = node.getRange();
      String inp = get(range, reader);

      LOGGER.debug("  - {}: range={}, input=\"{}\"", node.getNode().getName(), range, inp);
    });
  }

  private static String get(StringRange range, StringReader reader) {
    int start = Math.clamp(range.getStart(), 0, reader.getTotalLength());
    int end = Math.clamp(range.getEnd(), 0, reader.getTotalLength());
    return reader.getString().substring(start, end);
  }

  public static Plugin getCallingPlugin() {
    Class<?> callerClass = StackLocatorUtil.getCallerClass(3);
    return JavaPlugin.getProvidingPlugin(callerClass);
  }

  public interface ReaderPredicate {
    boolean parse(StringReader reader) throws CommandSyntaxException;
  }
}
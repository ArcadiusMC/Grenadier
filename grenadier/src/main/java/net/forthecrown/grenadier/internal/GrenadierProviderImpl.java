package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import net.forthecrown.grenadier.CommandExceptionHandler;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierProvider;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.AsyncCatcher;

@Getter
@Internal
public class GrenadierProviderImpl implements GrenadierProvider {

  private CommandExceptionHandler exceptionHandler
      = new DefaultExceptionHandler();

  private Plugin plugin;

  private final CommandDispatcher<CommandSource> dispatcher;

  private final ExceptionProviderImpl exceptionProvider
      = new ExceptionProviderImpl();

  public GrenadierProviderImpl() {
    this.dispatcher = new CommandDispatcher<>(new GrenadierRootNode(this));

    dispatcher.setConsumer((context, success, result) -> {
      context.getSource().onCommandComplete(context, success, result);
    });

    var loader = getClass().getClassLoader();
    if (loader instanceof PluginClassLoader pluginLoader) {
      var plugin = pluginLoader.getPlugin();

      if (plugin != null) {
        setPlugin(plugin);
      }
    }
  }

  public void setExceptionHandler(@NotNull CommandExceptionHandler exceptionHandler) {
    Objects.requireNonNull(exceptionHandler);
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public void setPlugin(@NotNull Plugin plugin) {
    Objects.requireNonNull(plugin);
    this.plugin = plugin;
  }

  @Override
  public Component fromMessage(Message message) {
    return MessageComponentSerializer.message().deserialize(message);
  }

  @Override
  public Message toMessage(Component component) {
    return MessageComponentSerializer.message().serialize(component);
  }

  @Override
  public CommandSource createSource(CommandSender sender) {
    Objects.requireNonNull(sender);

    return new CommandSourceImpl(
        VanillaCommandWrapper.getListener(sender)
    );
  }

  @Override
  public SuggestionProvider<CommandSource> suggestAllCommands() {
    return InternalUtil.SUGGEST_ALL_COMMANDS;
  }

  @Override
  public int dispatch(CommandSource source, String command) {
    Objects.requireNonNull(source, "Null source");
    Objects.requireNonNull(command, "Null command");

    AsyncCatcher.catchOp("Command execution");

    CommandSourceStack stack = InternalUtil.unwrap(source);
    AtomicInteger integer = new AtomicInteger();

    stack = stack.withCallback((successful, returnValue) -> {
      if (!successful) {
        return;
      }

      integer.set(returnValue);
    }, CommandResultCallback::chain);

    MinecraftServer.getServer().getCommands().dispatchServerCommand(stack, command);
    return integer.get();
  }
}
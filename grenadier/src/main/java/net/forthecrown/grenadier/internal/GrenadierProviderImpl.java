package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.brigadier.PaperBrigadier;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.grenadier.CommandExceptionHandler;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.command.VanillaCommandWrapper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

@Getter
@Internal
public class GrenadierProviderImpl implements GrenadierProvider {

  private CommandExceptionHandler exceptionHandler
      = new DefaultExceptionHandler();

  private Plugin plugin;

  private final CommandDispatcher<CommandSource> dispatcher;

  private final ExceptionProviderImpl exceptionProvider
      = new ExceptionProviderImpl();

  private CommandSyncListener syncListener;

  public GrenadierProviderImpl() {
    this.dispatcher = new CommandDispatcher<>(new GrenadierRootNode(this));

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

    if (syncListener == null) {
      syncListener = new CommandSyncListener(this);
      Bukkit.getPluginManager().registerEvents(syncListener, plugin);
    }
  }

  @Override
  public Component fromMessage(Message message) {
    return PaperBrigadier.componentFromMessage(message);
  }

  @Override
  public Message toMessage(Component component) {
    return PaperBrigadier.message(component);
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
}
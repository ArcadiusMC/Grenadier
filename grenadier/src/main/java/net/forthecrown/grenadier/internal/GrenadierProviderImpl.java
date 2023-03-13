package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import io.papermc.paper.brigadier.PaperBrigadier;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.grenadier.CommandExceptionHandler;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierProvider;
import net.forthecrown.grenadier.internal.types.ArgumentTypeProviderImpl;
import net.forthecrown.grenadier.types.ArgumentTypeProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R2.command.VanillaCommandWrapper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.PluginClassLoader;

@Getter
public class GrenadierProviderImpl implements GrenadierProvider {

  private CommandExceptionHandler exceptionHandler;

  private Plugin plugin;

  private final CommandDispatcher<CommandSource> dispatcher;

  private final ExceptionProviderImpl exceptionProvider
      = new ExceptionProviderImpl();

  public GrenadierProviderImpl() {
    this.dispatcher = new CommandDispatcher<>(new GrenadierRootNode(this));

    var loader = getClass().getClassLoader();
    if (loader instanceof PluginClassLoader pluginLoader) {
      setPlugin(pluginLoader.getPlugin());
    }
  }

  public void setExceptionHandler(CommandExceptionHandler exceptionHandler) {
    Objects.requireNonNull(exceptionHandler);
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public void setPlugin(Plugin plugin) {
    Objects.requireNonNull(plugin);
    this.plugin = plugin;
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
}
package net.forthecrown.grenadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Objects;
import net.forthecrown.grenadier.internal.GrenadierProviderImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class Grenadier {
  private Grenadier() {}

  private static GrenadierProvider provider;

  public static GrenadierProvider getProvider() {
    return provider == null
        ? (provider = new GrenadierProviderImpl())
        : provider;
  }

  public static CommandDispatcher<CommandSource> dispatcher() {
    return getProvider().getDispatcher();
  }

  public static CommandExceptionHandler exceptionHandler() {
    return getProvider().getExceptionHandler();
  }

  public static Plugin plugin() {
    return getProvider().getPlugin();
  }

  public static void plugin(Plugin plugin) {
    getProvider().setPlugin(plugin);
  }

  public static ExceptionProvider exceptions() {
    return getProvider().getExceptionProvider();
  }

  public static CommandSource createSource(CommandSender sender) {
    return getProvider().createSource(sender);
  }

  public static CommandSource createSource(CommandSender sender,
                                           GrenadierCommandNode node
  ) {
    var source = getProvider().createSource(sender);
    source.setCurrentNode(node);
    return source;
  }

  public static Component fromMessage(Message message) {
    return getProvider().fromMessage(message);
  }

  public static Message toMessage(Component component) {
    return getProvider().toMessage(component);
  }

  public static String fallbackPrefix() {
    return plugin() == null
        ? "grenadier"
        : plugin().getName().toLowerCase();
  }

  public static SuggestionProvider<CommandSource> suggestAllCommands() {
    return getProvider().suggestAllCommands();
  }

  public static GrenadierCommand createCommand(String name) {
    Objects.requireNonNull(name);
    return new GrenadierCommand(name.toLowerCase());
  }
}
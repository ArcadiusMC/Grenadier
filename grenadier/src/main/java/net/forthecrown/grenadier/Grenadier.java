package net.forthecrown.grenadier;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Objects;
import net.forthecrown.grenadier.internal.GrenadierProviderImpl;
import net.forthecrown.grenadier.internal.InternalUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grenadier singleton access
 * <p>
 * Note: When {@link #getProvider()} is called for the first time, the
 * provider will attempt to determine the plugin that loaded it. If a plugin is
 * found, then it is set as Grenadier's plugin and will be returned in
 * {@link #plugin()}.
 * <p>
 * If no plugin could be determined then there will be issues. See
 * {@link #plugin()} for why Grenadier requires a plugin
 */
public final class Grenadier {
  private Grenadier() {}

  // API provider, lazily initialized
  private static GrenadierProvider provider;

  /**
   * Gets or creates the grenadier provider
   * @return Grenadier API
   */
  public static GrenadierProvider getProvider() {
    return provider == null
        ? (provider = new GrenadierProviderImpl())
        : provider;
  }

  /**
   * Gets the command dispatcher.
   * <p>
   * This dispatcher has a restriction on the nodes registered into it, only
   * command nodes created by {@link GrenadierCommand} instances can be
   * registered into this dispatcher. More specifically, only
   * {@link GrenadierCommandNode}s can be registered into this dispatcher
   *
   * @return Grenadier dispatcher
   */
  public static CommandDispatcher<CommandSource> dispatcher() {
    return getProvider().getDispatcher();
  }

  /**
   * Executes a command.
   * <p>
   * Preferable over {@link Bukkit#dispatchCommand(CommandSender, String)} due to that method
   * ignoring values that may have been changed in the source's underlying {@link CommandSender}
   * object by a command such as
   * <br> {@code /execute positioned 12 10 12 run <command>}, <br>
   * which changes the location of the sender, but by using a {@link CommandSender} those modified
   * values are lost.
   *
   * @param source Source executing the command
   * @param command Command to execute
   *
   * @throws NullPointerException If either {@code source} or {@code command} are null
   *
   * @return Execution result, defined by {@link CommandDispatcher#execute(String, Object)}
   */
  public static int dispatch(@NotNull CommandSource source, @NotNull String command) {
    return getProvider().dispatch(source, command);
  }

  /**
   * Gets the plugin using grenadier.
   * <p>
   * If the grenadier singleton was created by a plugin class loader, then this
   * will return the plugin that loaded the Grenadier class.
   * <p>
   * A plugin is required to register an internal event listener that ensures
   * clients that connect to the server receive correct command trees, otherwise
   * the default bukkit command tree is sent to clients. The difference between
   * the two is mostly in the color of the text typed in chat.
   * <p>
   * Secondly, the listener also ensures that Grenadier is able to register
   * commands into the same dispatcher that the {@code /executes} command uses.
   * <p>
   * Thirdly, if no plugin is found, then there is a chance suggestions will not
   * function properly, this is mostly in the case of argument types like
   * {@link net.forthecrown.grenadier.types.options.OptionsArgument} that must
   * offset suggestions in a way the base Bukkit system cannot handle
   * <p>
   * If you're encountering any of the above-mentioned issues, place the
   * following in your plugin's {@code onEnable} method
   * <pre><code>
   * &#064;Override
   * public void onEnable() {
   *   Grenadier.plugin(this);
   * }
   * </code></pre>
   *
   * @return Grenadier plugin, {@code null}, if no plugin is set
   */
  public static Plugin plugin() {
    return getProvider().getPlugin();
  }

  /**
   * Sets the plugin using grenadier
   * @param plugin Plugin
   * @see #plugin(Plugin)
   */
  public static void plugin(@NotNull Plugin plugin) {
    getProvider().setPlugin(plugin);
  }

  /**
   * Gets the exception factory
   * <p>
   * Most, if not all, exceptions created by the returned factory are
   * translatable
   *
   * @return Exception factory
   */
  public static ExceptionProvider exceptions() {
    return getProvider().getExceptionProvider();
  }

  /**
   * Creates a command source for the specified {@code sender}
   * @param sender Sender to wrap
   * @return Created source
   */
  public static CommandSource createSource(CommandSender sender) {
    return getProvider().createSource(sender);
  }

  /**
   * Creates a command source for the specified {@code sender} and then calls
   * {@link CommandSource#setCurrentNode(GrenadierCommandNode)} with the
   * specified {@code node}
   *
   * @param sender Sender to wrap
   * @param node Current command node
   * @return Created source
   */
  public static CommandSource createSource(CommandSender sender,
                                           GrenadierCommandNode node
  ) {
    var source = getProvider().createSource(sender);
    source.setCurrentNode(node);
    return source;
  }

  /**
   * Converts a Brigadier message to a component.
   * <p>
   * This is preferable to using {@link Message#getString()} because the vanilla
   * implementation of the chat component system also implements {@link Message}.
   * So the supplied message may be a {@link com.mojang.brigadier.LiteralMessage}
   * or a vanilla component.
   *
   * @param message Message to convert
   * @return Component
   */
  public static Component fromMessage(Message message) {
    return getProvider().fromMessage(message);
  }

  /**
   * Converts the adventure component to a message.
   * <p>
   * Internally this turns the Adventure API chat component to a vanilla chat
   * component
   *
   * @param component Component to convert
   * @return Message
   */
  public static Message toMessage(Component component) {
    return getProvider().toMessage(component);
  }

  /**
   * Gets the command fallback prefix, this is prepended onto command labels to
   * prevent commands from overriding eachother.
   * <p>
   * Example: <pre>
   * Without prefix: command_label
   * With prefix:    fallback:command_label
   * </pre>
   * @return Grenadier fallback prefix
   */
  public static String fallbackPrefix() {
    return plugin() == null
        ? "grenadier"
        : plugin().getName().toLowerCase();
  }

  /**
   * Gets a suggestion provider that suggests all commands currently registered
   * on the server.
   * <p>
   * The returned suggestion provider not only suggests command labels, but
   * also gets suggestions specific to each command
   *
   * @return suggestion provider
   */
  public static SuggestionProvider<CommandSource> suggestAllCommands() {
    return getProvider().suggestAllCommands();
  }

  /**
   * Creates a grenadier command builder
   * @param name Name of the command
   * @return Created command
   */
  public static GrenadierCommand createCommand(@NotNull String name) {
    return new GrenadierCommand(name, InternalUtil.getCallingPlugin());
  }

  public static GrenadierCommand createCommand(@NotNull String name, @NotNull Plugin plugin) {
    return new GrenadierCommand(name, plugin);
  }

  /**
   * Gets a logger with the name 'Grenadier'
   * @return Grenadier's logger
   */
  public static Logger getLogger() {
    return LoggerFactory.getLogger("Grenadier");
  }

  /**
   * Ensures a specified command {@code label} is valid. For a command label to
   * be valid it must be non-null and not empty
   *
   * @param label Label to test
   * @throws IllegalArgumentException If the label was empty, or blank
   * @throws NullPointerException If the label was null
   */
  public static void ensureValidLabel(String label)
      throws IllegalArgumentException, NullPointerException
  {
    Objects.requireNonNull(label, "Null command label");

    Preconditions.checkArgument(!label.isBlank(),
        "Command label cannot be blank"
    );
  }
}
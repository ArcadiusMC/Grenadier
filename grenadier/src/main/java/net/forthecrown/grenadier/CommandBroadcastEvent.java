package net.forthecrown.grenadier;

import static net.kyori.adventure.text.Component.translatable;

import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CommandBroadcastEvent extends Event implements Cancellable {

  private static final HandlerList handlerList = new HandlerList();

  private final CommandSource source;
  private final Set<Audience> viewers;

  private Formatter formatter = Formatter.DEFAULT;
  private Component message;

  private boolean cancelled;

  public CommandBroadcastEvent(
      CommandSource source,
      Component message,
      Set<Audience> viewers
  ) {
    this.source = source;
    this.message = message;
    this.viewers = viewers;
  }

  /**
   * Gets the source of the broadcast
   * @return broadcast source
   */
  public CommandSource getSource() {
    return source;
  }

  /**
   * Gets the message of the announcement
   * @return Announcement message
   */
  public Component getMessage() {
    return message;
  }

  /**
   * Sets the announcement message
   * @param message new message
   */
  public void setMessage(Component message) {
    this.message = Objects.requireNonNull(message);
  }

  /**
   * Gets a mutable set of broadcast viewers.
   * <p>
   * Unless manually added, this set will not contain the player the
   * {@link #getSource()} represents, or, in the case the source is the console,
   * this set will not contain the console sender
   *
   * @return Viewers
   */
  public Set<Audience> getViewers() {
    return viewers;
  }

  /**
   * Gets the broadcast message formatter
   * @return Broadcast formatter
   */
  public Formatter getFormatter() {
    return formatter;
  }

  /**
   * Sets the broadcast message formatter
   * @param formatter message formatter
   */
  public void setFormatter(Formatter formatter) {
    this.formatter = Objects.requireNonNull(formatter);
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlerList;
  }

  /**
   * Formatter for a command broadcast
   */
  public interface Formatter {
    /**
     * Default broadcast formatter
     * <p>
     * Message example: <pre>
     * [Source: Message]
     * </pre>
     */
    Formatter DEFAULT = (audience, message, source) -> {
      return translatable("chat.type.admin", source.displayName(), message)
          .color(NamedTextColor.GRAY)
          .decorate(TextDecoration.ITALIC);
    };

    /**
     * Formats a broadcast message
     *
     * @param viewer Message viewer
     * @param message Base message
     * @param source Source that send the message
     * @return Formatted message
     */
    @NotNull
    Component formatMessage(Audience viewer,
                            Component message,
                            CommandSource source
    );
  }
}
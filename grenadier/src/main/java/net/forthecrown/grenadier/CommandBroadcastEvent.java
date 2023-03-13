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

  public CommandSource getSource() {
    return source;
  }

  public Component getMessage() {
    return message;
  }

  public void setMessage(Component message) {
    this.message = Objects.requireNonNull(message);
  }

  public Set<Audience> getViewers() {
    return viewers;
  }

  public Formatter getFormatter() {
    return formatter;
  }

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

  public interface Formatter {
    Formatter DEFAULT = (audience, message, source) -> {
      if (Objects.equals(audience, source.asBukkit())) {
        source.sendMessage(message);
      }

      return translatable("chat.type.admin", source.displayName(), message)
          .color(NamedTextColor.GRAY)
          .decorate(TextDecoration.ITALIC);
    };

    @NotNull
    Component formatMessage(Audience audience,
                            Component message,
                            CommandSource source
    );
  }
}
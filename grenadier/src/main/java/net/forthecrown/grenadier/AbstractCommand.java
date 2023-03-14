package net.forthecrown.grenadier;

import java.util.function.Predicate;
import org.bukkit.permissions.Permission;

public abstract class AbstractCommand
    implements CommandTreeBuilder, Predicate<CommandSource> {

  private final GrenadierCommand command;
  private boolean registered;

  public AbstractCommand(String name) {
    this.command = Grenadier.createCommand(name);
  }

  public final void register() {
    if (registered) {
      return;
    }

    command.requires(this);
    createCommand(command);
    command.register();

    registered = true;
  }

  @Override
  public boolean test(CommandSource source) {
    return true;
  }

  public GrenadierCommand getCommand() {
    return command;
  }

  public String getDescription() {
    return command.getDescription();
  }

  public AbstractCommand setDescription(String description) {
    command.withDescription(description);
    return this;
  }

  public String getPermission() {
    return command.getPermission();
  }

  public AbstractCommand setPermission(String permission) {
    command.withPermission(permission);
    return this;
  }

  public AbstractCommand setPermission(Permission permission) {
    command.withPermission(permission);
    return this;
  }
}
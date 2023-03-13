package net.forthecrown.grenadier;

import java.util.Objects;

public abstract class AbstractCommand {

  private final GrenadierCommand command;

  public AbstractCommand(String name) {
    this.command = new GrenadierCommand(
        Objects.requireNonNull(name).toLowerCase()
    );
  }

  public GrenadierCommand getCommand() {
    return command;
  }

  public String getDescription() {
    return command.getDescription();
  }

  public AbstractCommand setDescription(String description) {
    command.setDescription(description);
    return this;
  }
}
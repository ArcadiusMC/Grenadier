package net.forthecrown.grenadier.internal;

import org.bukkit.command.Command;
import org.bukkit.help.GenericCommandHelpTopic;
import org.jetbrains.annotations.NotNull;

public class GrenadierHelpTopic extends GenericCommandHelpTopic {

  public GrenadierHelpTopic(String name, @NotNull Command command) {
    super(command);
    this.name = "/" + name;
  }
}

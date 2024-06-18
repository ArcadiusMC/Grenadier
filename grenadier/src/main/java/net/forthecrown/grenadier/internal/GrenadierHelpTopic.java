package net.forthecrown.grenadier.internal;

import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.help.GenericCommandHelpTopic;
import org.jetbrains.annotations.NotNull;

public class GrenadierHelpTopic extends GenericCommandHelpTopic {

  private final GrenadierCommandNode command;

  public GrenadierHelpTopic(String name, @NotNull Command command, GrenadierCommandNode command1) {
    super(command);
    this.command = command1;
    this.name = "/" + name;
  }

  @Override
  public boolean canSee(@NotNull CommandSender sender) {
    if (!super.canSee(sender)) {
      return false;
    }

    CommandSource source = Grenadier.createSource(sender, command);
    return command.canUse(source);
  }
}

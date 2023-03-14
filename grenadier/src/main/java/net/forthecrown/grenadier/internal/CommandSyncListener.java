package net.forthecrown.grenadier.internal;

import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class CommandSyncListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onCommandRegistered(
      CommandRegisteredEvent<CommandSourceStack> event
  ) {
    if (!(event.getCommand() instanceof GrenadierBukkitWrapper wrapper)) {
      return;
    }

    GrenadierCommandData data = wrapper.getData();

    LiteralCommandNode<CommandSourceStack> tree
        = data.nmsTreeWith(event.getCommandLabel());

    event.setLiteral(tree);
  }
}
package net.forthecrown.grenadier.internal;

import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.forthecrown.grenadier.GrenadierProvider;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class CommandSyncListener implements Listener {

  private final GrenadierRootNode rootNode;

  public CommandSyncListener(GrenadierProvider provider) {
    this.rootNode = (GrenadierRootNode) provider.getDispatcher().getRoot();
  }

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
    rootNode.syncVanilla();
  }
}
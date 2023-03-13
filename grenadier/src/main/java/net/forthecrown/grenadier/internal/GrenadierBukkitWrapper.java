package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GrenadierBukkitWrapper extends Command {
  private final LiteralCommandNode<CommandSource> treeRoot;

  public GrenadierBukkitWrapper(LiteralCommandNode<CommandSource> treeRoot) {
    super(treeRoot.getLiteral());
    this.treeRoot = treeRoot;
  }

  @Override
  public boolean testPermissionSilent(@NotNull CommandSender target) {
    CommandSource source = Grenadier.createSource(target, treeRoot);
    return treeRoot.canUse(source);
  }

  @Override
  public boolean execute(@NotNull CommandSender sender,
                         @NotNull String commandLabel,
                         @NotNull String[] args
  ) {
    CommandSource source = Grenadier.createSource(sender, treeRoot);
    StringReader reader = InternalUtil.ofBukkit(commandLabel, args);

    InternalUtil.execute(source, reader);
    return true;
  }
}
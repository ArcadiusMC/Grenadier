package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.forthecrown.grenadier.CommandSource;

public class GrenadierRootNode extends RootCommandNode<CommandSource> {

  private final GrenadierProviderImpl provider;

  public GrenadierRootNode(GrenadierProviderImpl provider) {
    this.provider = provider;
  }

  public GrenadierProviderImpl getProvider() {
    return provider;
  }

  @Override
  public void removeCommand(String name) {
    super.removeCommand(name);
  }

  @Override
  public void addChild(CommandNode<CommandSource> node) {
    super.addChild(node);
  }
}
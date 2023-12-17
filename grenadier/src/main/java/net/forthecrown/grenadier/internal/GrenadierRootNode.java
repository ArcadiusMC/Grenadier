package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.minecraft.server.MinecraftServer;

class GrenadierRootNode extends RootCommandNode<CommandSource> {

  private final GrenadierProviderImpl provider;

  /**
   * Determines if {@link MinecraftServer#vanillaCommandDispatcher} has become a
   * separate instance from {@link MinecraftServer#getCommands()}. If it has,
   * then we can register our commands into the vanillaCommandDispatcher as we
   * wish without fear of Bukkit taking our vanilla Command trees and wrapping
   * them, which causes a whole host of issues
   * <p>
   * I don't know why those getCommands() and vanillaCommandDispatcher are
   * separated, but it prevents Bukkit commands from being used in the
   * execute command, so we have to sync to that dispatcher separately
   */
  private boolean vanillaSeparated = false;

  private final Map<String, GrenadierCommandData> dataMap = new HashMap<>();

  public GrenadierRootNode(GrenadierProviderImpl provider) {
    this.provider = provider;
  }

  public GrenadierProviderImpl getProvider() {
    return provider;
  }

  public GrenadierCommandData getData(String label) {
    return dataMap.get(label);
  }

  public void syncVanilla() {
    if (vanillaSeparated) {
      return;
    }

    Set<GrenadierCommandData> unique = new HashSet<>(dataMap.values());
    unique.forEach(GrenadierCommandData::registerVanilla);
    vanillaSeparated = true;
  }

  @Override
  public CommandNode<CommandSource> getChild(String name) {
    GrenadierCommandData data = dataMap.get(name);

    if (data != null) {
      return data.getNode();
    }

    return super.getChild(name);
  }

  @Override
  public void removeCommand(String name) {
    super.removeCommand(name);

    GrenadierCommandData data = dataMap.remove(name);

    if (data == null) {
      return;
    }

    data.getNode().forEachLabel(s -> {
      dataMap.remove(s, data);
    });

    data.unregister();
  }

  @Override
  public void addChild(CommandNode<CommandSource> node) {
    if (!(node instanceof GrenadierCommandNode grenadierNode)) {
      throw new IllegalArgumentException(
          "All command nodes registered into grenadier's dispatcher must "
              + "be GrenadierCommandNode instances"
      );
    }

    super.addChild(node);

    GrenadierCommandData data = new GrenadierCommandData(grenadierNode);
    grenadierNode.forEachLabel(s -> dataMap.putIfAbsent(s, data));

    data.register();

    if (vanillaSeparated) {
      data.registerVanilla();
    }

    if (provider.getPlugin() == null) {
      provider.setPlugin(grenadierNode.getPlugin());
    }
  }

  @Override
  public Collection<? extends CommandNode<CommandSource>> getRelevantNodes(
      StringReader input
  ) {
    List<GrenadierCommandNode> nodes = new ArrayList<>();
    final int start = input.getCursor();

    for (var c: getChildren()) {
      if (!(c instanceof GrenadierCommandNode node)) {
        continue;
      }

      int parseResult = node.parse(input);
      input.setCursor(start);

      if (parseResult >= 0) {
        nodes.add(node);
      }
    }

    return nodes;
  }
}
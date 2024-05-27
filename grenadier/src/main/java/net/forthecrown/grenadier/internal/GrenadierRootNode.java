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
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.help.MultipleCommandAliasHelpTopic;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;

class GrenadierRootNode extends RootCommandNode<CommandSource> {

  private final GrenadierProviderImpl provider;

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

    if (provider.getPlugin() == null && grenadierNode.getPlugin() != null) {
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
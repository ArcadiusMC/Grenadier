package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.Plugin;

@Getter
class GrenadierCommandData {

  private final Plugin plugin;

  private final GrenadierCommandNode node;
  private final GrenadierBukkitWrapper bukkitWrapper;
  private final LiteralCommandNode<CommandSourceStack> vanillaTree;
  private final String fallback;
  private final GenericCommandHelpTopic helpTopic;

  private final Set<String> registeredVanillaLabels;

  public GrenadierCommandData(GrenadierCommandNode node) {
    this.node = node;
    this.plugin = node.getPlugin();
    this.bukkitWrapper = new GrenadierBukkitWrapper(this);
    this.vanillaTree = TreeTranslator.translateLiteral(node, node);
    this.fallback = plugin == null ? Grenadier.fallbackPrefix() : plugin.getName();
    this.registeredVanillaLabels = new HashSet<>();
    this.helpTopic = new GenericCommandHelpTopic(bukkitWrapper);
  }

  public void registerVanilla() {
    MinecraftServer server = DedicatedServer.getServer();
    registerIntoVanilla(server.getCommands());
  }

  private void registerIntoVanilla(Commands commands) {
    unregisterFrom(commands);

    var dispatcher = commands.getDispatcher();

    node.forEachLabel(s -> {
      String withFallback = fallback + ":" + s;

      if (registerNodeInto(dispatcher, s)) {
        registeredVanillaLabels.add(s);
      }
      if (registerNodeInto(dispatcher, withFallback)) {
        registeredVanillaLabels.add(withFallback);
      }
    });
  }

  private boolean registerNodeInto(CommandDispatcher<CommandSourceStack> dispatcher, String label) {
    RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
    CommandNode<CommandSourceStack> child = root.getChild(label);

    // unwrappedCache being null means the node does NOT represent a bukkit command. I think?
    if (child == null || child.unwrappedCached == null) {
      root.removeCommand(label);
      root.addChild(getVanillaTree(label, false));

      return true;
    }

    return false;
  }

  public void unregister() {
    MinecraftServer server = DedicatedServer.getServer();
    unregisterFrom(server.getCommands());
  }

  private void unregisterFrom(Commands commands) {
    var root = commands.getDispatcher().getRoot();

    for (String label : registeredVanillaLabels) {
      root.removeCommand(label);
    }

    registeredVanillaLabels.clear();
  }

  public void register() {
    registerVanilla();

    // Hack used to get the HelpTopic to show up correctly,
    // changes absolutely nothing (as of 1.20.6) but makes
    // the command think it's been registered.
    CommandMap map = Bukkit.getCommandMap();
    bukkitWrapper.register(map);

    HelpMap helpMap = Bukkit.getHelpMap();

    node.labels().forEachRemaining(s -> {
      HelpTopic topic;

      if (helpMap.getHelpTopic(s) == null) {
        topic = new GrenadierHelpTopic(s, bukkitWrapper, node);
        helpMap.addTopic(topic);
      }

      if (helpMap.getHelpTopic(fallback + ":" + s) == null) {
        topic = new GrenadierHelpTopic(fallback + ":" + s, bukkitWrapper, node);
        helpMap.addTopic(topic);
      }
    });
  }

  /**
   * Produces a vanilla command tree that corresponds to
   * {@link GrenadierCommandNode#isPlainTranslation()}
   *
   * @param label Command label
   * @return Created command tree
   */
  public LiteralCommandNode<CommandSourceStack> getVanillaTree(String label) {
    return getVanillaTree(label, node.isPlainTranslation());
  }

  public LiteralCommandNode<CommandSourceStack> getVanillaTree(String label, boolean plain) {
    var tree = createSimpleTree(label);

    if (plain) {
      return tree;
    }

    tree.clientNode = nmsTreeWith(label);
    return tree;
  }

  public LiteralCommandNode<CommandSourceStack> nmsTreeWith(String label) {
    return withLabel(vanillaTree, label);
  }

  public LiteralCommandNode<CommandSourceStack> createSimpleTree(
      String label
  ) {
    LiteralArgumentBuilder<CommandSourceStack> literal
        = LiteralArgumentBuilder.literal(label);

    literal.executes(TreeTranslator.COMMAND)
        .requires(vanillaTree.getRequirement());

    RequiredArgumentBuilder<CommandSourceStack, String> req
        = RequiredArgumentBuilder.argument("args", StringArgumentType.greedyString());

    req.suggests(TreeTranslator.SUGGESTION_PROVIDER)
        .executes(TreeTranslator.COMMAND);

    return literal.then(req).build();
  }

  public static LiteralCommandNode<CommandSourceStack> withLabel(
      LiteralCommandNode<CommandSourceStack> original,
      String label
  ) {
    LiteralArgumentBuilder<CommandSourceStack> literal
        = LiteralArgumentBuilder.literal(label);

    literal.requires(original.getRequirement())
        .executes(original.getCommand());

    if (original.getRedirect() != null) {
      literal.forward(
          original.getRedirect(),
          original.getRedirectModifier(),
          original.isFork()
      );
    }

    for (var c: original.getChildren()) {
      literal.then(c);
    }

    return literal.build();
  }
}
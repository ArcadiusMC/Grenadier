package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.Getter;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

@Getter
class GrenadierCommandData {

  private final GrenadierCommandNode node;
  private final GrenadierBukkitWrapper bukkitWrapper;
  private final LiteralCommandNode<CommandSourceStack> vanillaTree;
  private final String fallback;

  public GrenadierCommandData(GrenadierCommandNode node) {
    this.node = node;
    this.bukkitWrapper = new GrenadierBukkitWrapper(this);

    this.vanillaTree = (LiteralCommandNode<CommandSourceStack>)
        TreeTranslator.translateLiteral(node, node);

    this.fallback = Grenadier.fallbackPrefix();
  }

  public void registerVanilla() {
    MinecraftServer server = DedicatedServer.getServer();
    var vanilla = server.vanillaCommandDispatcher;

    unregisterFrom(vanilla);

    var root = vanilla.getDispatcher().getRoot();

    node.forEachLabel(s -> {
      root.addChild(nmsTreeWith(s));
      root.addChild(nmsTreeWith(fallback + ":" + s));
    });
  }

  public void unregister() {
    CommandMap map = Bukkit.getCommandMap();
    MinecraftServer server = DedicatedServer.getServer();

    unregisterFrom(server.vanillaCommandDispatcher);
    unregisterFrom(server.getCommands());

    node.forEachLabel(s -> {
      map.getKnownCommands().remove(fallback + ":" + s, bukkitWrapper);
      map.getKnownCommands().remove(s, bukkitWrapper);
    });
  }

  private void unregisterFrom(Commands commands) {
    var root = commands.getDispatcher().getRoot();

    node.forEachLabel(s -> {
      root.removeCommand(s);
      root.removeCommand(fallback + ":" + s);
    });
  }

  public void register() {
    CommandMap map = Bukkit.getCommandMap();
    map.register(fallback, bukkitWrapper);
  }

  public LiteralCommandNode<CommandSourceStack> nmsTreeWith(String label) {
    return withLabel(vanillaTree, label);
  }

  public static LiteralCommandNode<CommandSourceStack> withLabel(
      CommandNode<CommandSourceStack> original,
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
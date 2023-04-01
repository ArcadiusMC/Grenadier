package net.forthecrown.grenadier.internal;

import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierProvider;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.SyntaxExceptions;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.command.UnknownCommandEvent;

// Dear lord I absolutely hate this part of Grenadier, not the grenadier part,
// but the absolute sh*t I've had to work around to make it work in Bukkit.
//
// Want to have suggestions that don't start at the last argument separator?
// Nope! Bukkit's primitive-ahh suggestions don't allow for that, so either
// don't use Bukkit's system, or build a system to adapt to Bukkit's system.
// I'm not adapting to trash when a better way exists.
//
// Want to throw Command Syntax Exceptions? Nope! If a non-vanilla command does
// that for any reason, it gets straight up ignored and an 'Unknown Command'
// message pops up instead... WHYYYYY
// Who actually decided that was a smart idea?????
//
// Dear lord I hate absolutely everything about the way Bukkit does commands. If
// there was one feature I would absolutely beg Bukkit get rid of and ignore all
// needs for backwards compatibility and API compatibility, it would be this old
// command system that Mojang got rid of in 1.13 because they realized they
// could do so much more with a more dynamic command system.
//
// Bless you Mojang, for you doing that. But god-damn you Bukkit for doing
// everything in your ability to not abandon this outdated command system.
//
// With each new Minecraft version I'm bombarded by a cavalcade of bad decisions
// done behind the scenes of Bukkit that I have to trudge through to maintain
// this command engine. But I refuse to abandon this or make any compromise with
// Bukkit's system.
//
// Sorry... I had to vent my frustration with the command system somewhere,
// so I chose here
//
// - Jules
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

    // Raw commands use Bukkit's suggestions. Absolutely not accepting that
    event.setRawCommand(false);

    GrenadierCommandData data = wrapper.getData();

    LiteralCommandNode<CommandSourceStack> tree
        = data.nmsTreeWith(event.getCommandLabel());

    event.setLiteral(tree);
    rootNode.syncVanilla();
  }

  @EventHandler(ignoreCancelled = true)
  public void onUnknownCommand(UnknownCommandEvent event) {
    StringReader reader = Readers.createFiltered(event.getCommandLine());
    CommandSource source = Grenadier.createSource(event.getSender());

    var relevantNodes = rootNode.getRelevantNodes(reader);

    if (relevantNodes.isEmpty()) {
      return;
    }

    ParseResults<CommandSource> results
        = Grenadier.dispatcher().parse(reader, source);

    var exceptions = results.getExceptions();
    CommandSyntaxException exc;

    if (exceptions.isEmpty()) {
      exc = CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .dispatcherUnknownCommand()
          .createWithContext(reader);
    } else if (exceptions.size() == 1) {
      exc = exceptions.values().iterator().next();
    } else {
      exc = CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .dispatcherUnknownArgument()
          .createWithContext(reader);
    }

    Component message = SyntaxExceptions.formatCommandException(exc);
    event.message(message);
  }
}
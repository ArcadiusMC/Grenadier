package net.forthecrown.grenadier.internal;

import com.google.common.base.Strings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import java.util.ArrayList;
import java.util.List;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

class GrenadierBukkitWrapper extends Command implements PluginIdentifiableCommand {

  private final GrenadierCommandNode treeRoot;
  private final GrenadierCommandData data;

  public GrenadierBukkitWrapper(GrenadierCommandData data) {
    super(data.getNode().getLiteral());
    this.treeRoot = data.getNode();
    this.data = data;

    setAliases(treeRoot.getAliases());
    setDescription(Strings.nullToEmpty(treeRoot.getDescription()));
    setPermission(treeRoot.getPermission());
    setName(treeRoot.getName());
  }

  public GrenadierCommandData getData() {
    return data;
  }

  @Override
  public @NotNull Plugin getPlugin() {
    return data.getPlugin();
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
    StringReader reader = InternalUtil.bukkitReader(commandLabel, args);

    InternalUtil.execute(source, reader);
    return true;
  }

  @Override
  public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
                                           @NotNull String alias,
                                           @NotNull String[] args
  ) throws IllegalArgumentException {
    CommandSource source = Grenadier.createSource(sender, treeRoot);
    StringReader reader = InternalUtil.bukkitReader(alias, args);

    CommandDispatcher<CommandSource> dispatcher = Grenadier.dispatcher();
    ParseResults<CommandSource> results = dispatcher.parse(reader, source);

    List<String> result = new ArrayList<>();

    dispatcher.getCompletionSuggestions(results).thenAccept(suggestions -> {
      String input = reader.getString();

      for (Suggestion suggestion : suggestions.getList()) {
        String before = input.substring(0, suggestion.getRange().getStart());

        if (before.endsWith(" ")) {
          result.add(suggestion.getText());
        } else {
          int lastSpace = before.lastIndexOf(' ');
          String prefix = before.substring(lastSpace + 1);
          result.add(prefix + suggestion.getText());
        }
      }
    });

    return result;
  }
}
package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import java.util.ArrayList;
import java.util.List;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class GrenadierBukkitWrapper extends Command {

  private final GrenadierCommandNode treeRoot;
  private final GrenadierCommandData data;

  public GrenadierBukkitWrapper(GrenadierCommandData data) {
    super(data.getNode().getLiteral());
    this.treeRoot = data.getNode();
    this.data = data;
  }

  public GrenadierCommandData getData() {
    return data;
  }

  @Override
  public @Nullable String getPermission() {
    return treeRoot.getPermission();
  }

  @Override
  public @NotNull String getDescription() {
    return treeRoot.getDescription();
  }

  @Override
  public @NotNull List<String> getAliases() {
    return treeRoot.getAliases();
  }

  @Override
  public @NotNull String getName() {
    return treeRoot.getName();
  }

  @Override
  public @NotNull String getLabel() {
    return treeRoot.getLiteral();
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

  @Override
  public @NotNull List<String> tabComplete(@NotNull CommandSender sender,
                                           @NotNull String alias,
                                           @NotNull String[] args
  ) throws IllegalArgumentException {
    CommandSource source = Grenadier.createSource(sender, treeRoot);
    StringReader reader = InternalUtil.ofBukkit(alias, args);

    CommandDispatcher<CommandSource> dispatcher = Grenadier.dispatcher();
    ParseResults<CommandSource> results = dispatcher.parse(reader, source);

    List<String> result = new ArrayList<>();

    dispatcher.getCompletionSuggestions(results).thenAccept(suggestions -> {
      suggestions.getList().forEach(suggestion -> {
        result.add(suggestion.getText());
      });
    });

    return result;
  }
}
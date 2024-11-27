package net.forthecrown.grenadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface GrenadierProvider {

  @NotNull
  CommandExceptionHandler getExceptionHandler();

  void setExceptionHandler(@NotNull CommandExceptionHandler handler);

  @NotNull
  CommandDispatcher<CommandSource> getDispatcher();

  Plugin getPlugin();

  void setPlugin(@NotNull Plugin plugin);

  @NotNull
  ExceptionProvider getExceptionProvider();

  Component fromMessage(Message message);

  Message toMessage(Component component);

  CommandSource createSource(CommandSender sender);

  SuggestionProvider<CommandSource> suggestAllCommands();

  int dispatch(CommandSource source, String command);

  void enqueueCommand(CommandSource source, String command);

  void reregisterAll();
}
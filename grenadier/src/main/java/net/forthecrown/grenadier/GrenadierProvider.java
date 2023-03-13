package net.forthecrown.grenadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
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

  @NotNull
  Plugin getPlugin();

  void setPlugin(@NotNull Plugin plugin);

  @NotNull
  ExceptionProvider getExceptionProvider();

  Component fromMessage(Message message);

  Message toMessage(Component component);

  CommandSource createSource(CommandSender sender);
}
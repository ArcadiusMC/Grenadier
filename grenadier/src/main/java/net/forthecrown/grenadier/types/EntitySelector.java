package net.forthecrown.grenadier.types;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface EntitySelector {

  Player findPlayer(CommandSource source) throws CommandSyntaxException;

  Entity findEntity(CommandSource source) throws CommandSyntaxException;

  List<Player> findPlayers(CommandSource source) throws CommandSyntaxException;

  List<Entity> findEntities(CommandSource source) throws CommandSyntaxException;

  boolean isSelfSelector();

  boolean isWorldLimited();

  boolean includesEntities();

  int getMaxResults();
}
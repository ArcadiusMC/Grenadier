package net.forthecrown.grenadier.types;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Parsed representation of an entity selector.
 * <p>
 * Before any method returns its result, the results are filtered using
 * {@link CommandSource#canSee(Entity)}
 */
public interface EntitySelector {

  /**
   * Finds a single player.
   * @param source Source accessing this selector
   * @return The found player
   * @throws CommandSyntaxException If the source isn't allowed to use entity
   *                                selectors, or if no visible player was found
   */
  Player findPlayer(CommandSource source) throws CommandSyntaxException;

  /**
   * Finds a single entity
   * @param source Source accessing this selector
   * @return Found entity
   * @throws CommandSyntaxException If the source isn't allowed to use entity
   *                                selectors, or if no visible entity was found
   */
  Entity findEntity(CommandSource source) throws CommandSyntaxException;

  /**
   * Finds multiple players
   * @param source Source accessing this selector
   * @return Found players, or an empty list, if no valid visible players were
   *         found
   * @throws CommandSyntaxException If the source isn't allowed to use entity
   *                                selectors
   */
  List<Player> findPlayers(CommandSource source) throws CommandSyntaxException;

  /**
   * Finds multiple entities
   * @param source Source accessing this selector
   * @return Found entities, or an empty list, if no valid visible entities were
   *         found
   * @throws CommandSyntaxException If the source isn't allowed to use entity
   *                                selectors
   */
  List<Entity> findEntities(CommandSource source) throws CommandSyntaxException;

  /**
   * Tests if this selector is a self selector
   * @return If the parsed selector was a {@code @s} selector
   */
  boolean isSelfSelector();

  /**
   * Tests if this selector is limited by world
   * @return {@code true}, if this selector is limited by world,
   *         {@code false} otherwise
   */
  boolean isWorldLimited();

  /**
   * Tests if this selector includes all entities, or only players
   * @return {@code true} if all entities are selectable,
   *         {@code false} if just players are selectable
   */
  boolean includesEntities();

  /**
   * Gets the maximum amount of results
   * @return max results
   */
  int getMaxResults();
}
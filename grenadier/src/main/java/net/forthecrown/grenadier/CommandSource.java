package net.forthecrown.grenadier;

import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.entity.LookAnchor;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.forthecrown.grenadier.types.CoordinateSuggestion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The source of command execution.
 * <p>
 * A command source isn't a player or entity directly, rather, it's more of a
 * snapshot of the backing player or sender from the moment they execute a given
 * command. And while the object itself is immutable, there are functions that
 * allow you modify the return values of certain methods, eg:
 * {@link #withLocation(Location)}
 */
public interface CommandSource
    extends ResultConsumer<CommandSource>,
            ServerOperator,
            ForwardingAudience.Single
{

  /**
   * Gets the normal Bukkit CommandSender
   *
   * @return the CommandSender of this source
   */
  CommandSender asBukkit();

  /**
   * Checks if the sender is of the type
   * <p>
   * For example, to use this to check if the sender is a player you'd use
   * <code>boolean isPlayer = is(Player.class);</code>
   *
   * @param clazz The class of the type to check
   * @param <T>
   * @return Whether the sender is of the type
   */
  default <T extends CommandSender> boolean is(@NotNull Class<T> clazz) {
    Objects.requireNonNull(clazz);
    return clazz.isInstance(asBukkit());
  }

  /**
   * Gets the sender as the specified type
   * <p>
   * To use this to get the sender as, for example, a slime, you'd do:
   * <code>Slime slime = as(Slime.class);</code>
   *
   * @param clazz The class of the type, must extend {@link CommandSender}
   * @param <T>   The type
   * @return The sender as the type
   * @throws CommandSyntaxException If the sender is not of the specified type
   */
  default <T extends CommandSender> T as(Class<T> clazz) throws CommandSyntaxException {
    return optionalAs(clazz).orElseThrow(() -> Grenadier.exceptions().sourceMustBe(clazz));
  }

  /**
   * Similar to {@link CommandSource#as(Class)} however it won't throw an
   * exception if the sender is not of the given type, rather it just returns
   * null
   *
   * @param clazz The class of the type, must extend {@link CommandSender}
   * @param <T>   The type
   * @return The sender as the given type, or null if sender isn't of the given
   * type
   */
  default @Nullable <T extends CommandSender> T asOrNull(Class<T> clazz) {
    return optionalAs(clazz).orElse(null);
  }

  /**
   * Creates an optional that's empty if this source's backing
   * {@link CommandSender} is not of the given type, if the sender is of the
   * given type, then the optional will contain the sender casted to the given
   * type
   *
   * @param clazz The type to get the sender as
   * @param <T>   The type
   * @return The created optional, empty if sender isn't an instance of the
   * given class, otherwise, contains the sender cast to that type.
   */
  default @NotNull <T extends CommandSender> Optional<T> optionalAs(Class<T> clazz) {
    return is(clazz) ? Optional.of(clazz.cast(asBukkit())) : Optional.empty();
  }

  /**
   * Checks if the source is a player
   *
   * @return Whether the sender is a player or not
   */
  default boolean isPlayer() {
    return is(Player.class);
  }

  /**
   * Checks if the source is an entity
   *
   * @return True, if the source is an entity
   */
  default boolean isEntity() {
    return is(Entity.class);
  }

  /**
   * Gets the source as a player
   *
   * @return The player for this source
   * @throws CommandSyntaxException If the source is not a player
   */
  default Player asPlayer() throws CommandSyntaxException {
    return as(Player.class);
  }

  /**
   * Gets the source as a player, or null
   *
   * @return The player for this source, or null, if source is not a player
   */
  default @Nullable Player asPlayerOrNull() {
    return asOrNull(Player.class);
  }

  /**
   * Gets the source as an entity
   *
   * @return The entity of this source
   * @throws CommandSyntaxException If the source is not an entity
   */
  default Entity asEntity() throws CommandSyntaxException {
    return as(Entity.class);
  }

  /**
   * Gets the source as an entity, or null
   *
   * @return The entity of this source,o or null, if source is not an entity
   */
  default @Nullable Entity asEntityOrNull() {
    return asOrNull(Entity.class);
  }

  /**
   * Gets the display name of this source
   *
   * @return The source's display name
   */
  Component displayName();

  /**
   * Gets the string representation of the display name
   *
   * @return The string display name
   */
  String textName();

  /**
   * Gets the sender's location
   *
   * @return The sender's location
   */
  Location getLocation();

  /**
   * Gets the location of the source with the {@link #getAnchor()} vertical
   * offset applied.
   * <p>
   * If the {@link #getAnchor()} is {@link LookAnchor#FEET}, or the source is
   * not an entity then this will just return {@link #getLocation()}
   *
   * @return The source's anchored location.
   */
  Location getAnchoredLocation();

  /**
   * Gets the source's anchor point, either their feet or eyes.
   *
   * @return The source's anchor point
   */
  @Nullable
  LookAnchor getAnchor();

  /**
   * Gets the world the sender is in
   *
   * @return The sender's world
   */
  World getWorld();

  /**
   * Gets the server
   *
   * @return The server lol
   */
  Server getServer();

  /**
   * Checks if the sender has the given permission
   *
   * @param s The permission to check for
   * @return Whether they have the permission or not
   */
  boolean hasPermission(String s);

  /**
   * Checks if the sender has the given permission
   *
   * @param permission The permission to check for
   * @return Whether they have the permission or not
   */
  default boolean hasPermission(Permission permission) {
    return hasPermission(permission.getName());
  }

  /**
   * Checks if the sender has the vanilla OP level
   * <p>
   * Constants for levels are in {@link PermissionLevel}
   *
   * @param level The level to check for
   * @return Whether they have the given level of permissions
   * @see PermissionLevel
   */
  boolean hasPermission(PermissionLevel level);

  /**
   * Checks if the sender has both the bukkit permission and the OP permission
   * level
   *
   * @param perm  Level to check
   * @param level Permission to check
   * @return Whether the sender has both
   */
  default boolean hasPermission(String perm, PermissionLevel level) {
    return hasPermission(perm) && hasPermission(level);
  }

  /**
   * Gets the source's permission level
   * @return Permission level
   */
  PermissionLevel getPermissionLevel();

  /**
   * Checks if the sender is opped
   *
   * @return Whether the sender is opped or not
   */
  boolean isOp();

  /**
   * Tests if this source can see the specified {@code entity}
   * <p>
   * If this source is not a player, this will return true, otherwise this
   * method calls {@link Player#canSee(Entity)} to get the final result
   *
   * @param entity Entity to test
   * @return {@code true}, if this source can see the entity,
   *         {@code false} otherwise
   */
  default boolean canSee(Entity entity) {
    if (!isPlayer()) {
      return true;
    }

    return asPlayerOrNull().canSee(entity);
  }

  /**
   * Sends a message to the sender
   *
   * @param s The message to send
   */
  default void sendMessage(String s) {
    sendMessage(
        LegacyComponentSerializer.legacySection().deserialize(s)
    );
  }

  /**
   * Sends several messages to the sender lol
   *
   * @param s The messages to send
   */
  default void sendMessage(String... s) {
    for (String ss : s) {
      sendMessage(ss);
    }
  }

  /**
   * Broadcasts a message to other admins. The specified {@code message} won't
   * be sent to this source, or the underlying console/player sender of this
   * source
   *
   * @param message The message to broadcast
   */
  void broadcastAdmin(Component message);

  /**
   * Gets the current command the sender is using, null if no command is
   * currently in use
   *
   * @return The command the sender is currently using
   */
  @Nullable GrenadierCommandNode getCurrentNode();

  /**
   * Sets the current command the sender is using
   *
   * @param command The command the sender will be using
   */
  void setCurrentNode(@Nullable GrenadierCommandNode command);

  /**
   * Gets if the sender should broadcast admin messages
   *
   * @return Whether this sender should be broadcasting admin messages
   */
  boolean shouldInformAdmins();

  /**
   * Gets a coordinate suggestion relevant to this source, or null, if no
   * relevant cords were found in a 5 block distance
   *
   * @return Gets the cords of the spot the source is looking at
   */
  @Nullable CoordinateSuggestion getRelevant3DCords();

  /**
   * Gets a coordinate suggestion relevant to this source, or null, if no
   * relevant cords were found in a 5 block distance
   *
   * @return Gets the cords of the spot the source is looking at
   */
  @Nullable CoordinateSuggestion getRelevant2DCords();

  /**
   * Gets all entities this source may be looking at and returns their UUIDs for
   * suggestions.
   *
   * @return The IDs of all entities this source is looking at.
   */
  Collection<String> getEntitySuggestions();

  /**
   * Gets all players visible to this source
   * <p>
   * 'visibility' is determined by the {@link #canSee(Entity)} method
   *
   * @return All players visible to this source
   * @see #canSee(Entity)
   */
  @SuppressWarnings("unchecked") // Literally nothing here is unchecked
  default Stream<Player> getVisiblePlayers() {
    return (Stream<Player>) Bukkit.getOnlinePlayers()
        .stream()
        .filter(this::canSee);
  }

  /**
   * Checks if this source is 'silent', meaning it doesn't accept any messages
   *
   * @return True, if silent, false otherwise
   */
  boolean isSilent();

  /**
   * Checks whether this source accepts command success messages
   *
   * @return Whether this source accepts success messages
   */
  boolean acceptsSuccessMessage();

  /**
   * Checks whether this source accepts command failure messages
   *
   * @return Whether this source accepts failure messages
   */
  boolean acceptsFailureMessage();

  /**
   * Sends a success message
   *
   * @param msg The message to send
   */
  default void sendSuccess(Component msg) {
    sendSuccess(msg, true);
  }

  /**
   * Send a command success message
   *
   * @param msg       The message to send
   * @param broadcast Whether to broadcast that message to other OPs
   */
  default void sendSuccess(Component msg, boolean broadcast) {
    if (!isSilent() && acceptsSuccessMessage()) {
      sendMessage(msg);
    }

    if (broadcast && !isSilent() && shouldInformAdmins()) {
      broadcastAdmin(msg);
    }
  }

  /**
   * Send a command failure message
   *
   * @param msg The message to send
   */
  default void sendFailure(Component msg) {
    sendFailure(msg, false);
  }

  /**
   * Send a command failure message
   *
   * @param msg       The message to send
   * @param broadcast Whether to broadcast that message to other OPs
   */
  default void sendFailure(Component msg, boolean broadcast) {
    if (!isSilent() && acceptsFailureMessage()) {
      sendMessage(msg);
    }

    if (broadcast && !isSilent() && shouldInformAdmins()) {
      broadcastAdmin(msg);
    }
  }

  @Override
  default @NotNull Audience audience() {
    if (isSilent()) {
      return Audience.empty();
    }

    return asBukkit();
  }

  /**
   * Creates a silent command source
   * <p>
   * If {@link #isSilent()} already returns true, this will just return itself.
   *
   * @return A command source that accepts no message output
   */
  CommandSource silent();

  /**
   * Creates a command source at the given location.
   * <p>
   * This is a delegate method that chains {@link #withPosition(Vector)},
   * {@link #withWorld(World)} and {@link #withRotation(float, float)} together
   *
   * @param location The new location of the source
   * @return A source with the given location
   */
  default CommandSource withLocation(Location location) {
    return withWorld(location.getWorld())
        .withPosition(location.toVector())
        .withRotation(location.getYaw(), location.getPitch());
  }

  /**
   * Creates a command source at the given position.
   * <p>
   * Note: This does NOT move or teleport the source, it just changes the return
   * value of {@link #getLocation()}
   *
   * @param vector The position to move the source to
   * @return A source at the given position
   */
  CommandSource withPosition(Vector vector);

  /**
   * Creates a command source in the given world. If the given world is already
   * the source's world, this just returns itself.
   * <p>
   * Note: This does NOT move or teleport the source, it just changes the return
   * values of {@link #getWorld()} and {@link #getWorld()}
   *
   * @param world The world the source is in
   * @return A source in the given world
   */
  CommandSource withWorld(World world);

  /**
   * Creates a command source that's facing the given vector.
   * <p>
   * Note: This does NOT move or teleport the source, just changes the return
   * result of {@link #getLocation()}
   *
   * @param vector The Position the source should be facing
   * @return A source facing the given vector.
   */
  CommandSource facing(Vector vector);

  /**
   * Creates a command source that's rotated with the given yaw and pitch
   * <p>
   * Note: This does NOT move or teleport the source, just changes the return
   * result of {@link #getLocation()}
   *
   * @param yaw   The source's new yaw
   * @param pitch The source's new pitch
   * @return A source with the given rotation
   */
  CommandSource withRotation(float yaw, float pitch);

  /**
   * Creates a command sender with the given command sender.
   * <p>
   * If the given sender is already returned by {@link #asBukkit()}, then this
   * will return itself
   *
   * @param sender The sender to use
   * @return A command sender with the given sender
   */
  CommandSource withOutput(CommandSender sender);

  /**
   * Creates a copy of this command source that has the specified {@code level}
   * @param level new permission level
   * @return Copied command source with the specified level
   */
  CommandSource withPermissionLevel(PermissionLevel level);

  /**
   * Returns either a new source with the specified permission level, or this
   * source, if it already has the specified {@code level}
   * @param level Permission level
   * @return This, or a new source, if {@link #hasPermission(PermissionLevel)}
   *         failed
   */
  default CommandSource withMaximumLevel(PermissionLevel level) {
    if (hasPermission(level))  {
      return this;
    }

    return withPermissionLevel(level);
  }

  /**
   * Creates a command source facing the given location.
   * <p>
   * Note: This will NOT move or teleport the source, it simply changes the
   * return value of {@link #getLocation()}
   *
   * @param location The location to face
   * @return A command sender facing the given location
   */
  default CommandSource facing(Location location) {
    return facing(location.toVector());
  }

  /**
   * Creates a command source facing the given entity's location
   *
   * @param entity The entity to face.
   * @return A command source facing the given entity.
   */
  default CommandSource facing(Entity entity) {
    return facing(entity.getLocation());
  }

  /**
   * Creates a command source facing the given entity's anchor look point.
   *
   * @param entity The entity to face
   * @param anchor The anchor point to face
   * @return A command source facing the given entity
   */
  default CommandSource facing(LivingEntity entity, LookAnchor anchor) {
    return facing(
        anchor == LookAnchor.EYES
            ? entity.getEyeLocation()
            : entity.getLocation()
    );
  }

  /**
   * Creates a command source facing the given block
   *
   * @param block The block to face
   * @return A command source facing the given block
   */
  default CommandSource facing(Block block) {
    return facing(block.getLocation());
  }

  /**
   * Creates a command source with the given callback added to it. This callback
   * is called when the source's command execution is finished.
   *
   * @param consumer The consumer to use
   * @return A command source with the given consumer added to it.
   */
  CommandSource addCallback(ResultConsumer<CommandSource> consumer);

  /**
   * Tests if this source has selector permission overriding enabled
   * @return {@code true}, if override enabled, {@code false} otherwise
   */
  boolean overrideSelectorPermissions();
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.ArgumentTypeMapper;
import net.forthecrown.grenadier.types.RegistryArgument.UnknownFactory;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class ArgumentTypes {
  private ArgumentTypes() {}

  /**
   * Gets the item argument
   * @return Item argument
   */
  public static ItemArgument item() {
    return ItemArgumentImpl.INSTANCE;
  }

  /**
   * Gets the item filter argument
   * @return Item filter argument
   */
  public static ItemFilterArgument itemFilter() {
    return ItemFilterArgumentImpl.INSTANCE;
  }

  /**
   * Gets the chat component argument
   * @return Chat component argument
   */
  public static ComponentArgument component() {
    return ComponentArgumentImpl.INSTANCE;
  }

  /**
   * Gets the namespaced key argument
   * @return Namespaced key argument
   */
  public static KeyArgument key() {
    return KeyArgumentImpl.INSTANCE;
  }

  /**
   * Gets the world argument
   * @return World argument
   */
  public static WorldArgument world() {
    return WorldArgumentImpl.INSTANCE;
  }

  /**
   * Gets the binary tag argument
   * @return Binary tag argument
   */
  public static NbtArgument<BinaryTag> binaryTag() {
    return NbtArgumentImpl.BINARY_TAG;
  }

  /**
   * Gets the compound tag argument
   * @return Compound tag argument
   */
  public static NbtArgument<CompoundTag> compoundTag() {
    return NbtArgumentImpl.COMPOUND;
  }

  /**
   * Gets the NBT path argument
   * @return NBT path argument
   */
  public static TagPathArgument tagPath() {
    return TagPathArgumentImpl.INSTANCE;
  }

  /**
   * Gets the gamemode argument
   * @return Gamemode argument
   */
  public static GameModeArgument gameMode() {
    return GameModeArgumentImpl.INSTANCE;
  }

  /**
   * Gets the time duration argument
   * @return Time duration argument
   */
  public static TimeArgument time() {
    return TimeArgumentImpl.INSTANCE;
  }

  /**
   * Gets a parsed duration
   * @param context Command context
   * @param argument Name of the argument
   * @return Found duration
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't a {@link Duration}
   */
  public static Duration getDuration(CommandContext<?> context, String argument)
      throws IllegalArgumentException
  {
    return context.getArgument(argument, Duration.class);
  }

  /**
   * Gets a parsed duration, in milliseconds
   * @param context Command context
   * @param argument Name of the argument
   * @return Found duration, in milliseconds
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't a {@link Duration}
   */
  public static long getMillis(CommandContext<?> context, String argument)
      throws IllegalArgumentException
  {
    return getDuration(context, argument).toMillis();
  }

  /**
   * Gets a parsed duration, in game ticks
   * @param context Command context
   * @param argument Name of the argument
   * @return Found duration, in game ticks
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't a {@link Duration}
   */
  public static long getTicks(CommandContext<?> context, String argument)
      throws IllegalArgumentException
  {
    return getMillis(context, argument) / Ticks.SINGLE_TICK_DURATION_MS;
  }

  /**
   * Gets the scoreboard objective argument
   * @return Scoreboard objective argument
   */
  public static ObjectiveArgument objective() {
    return ObjectiveArgumentImpl.INSTANCE;
  }

  /**
   * Gets the scoreboard team argument
   * @return Scoreboard team argument
   */
  public static TeamArgument team() {
    return TeamArgumentImpl.INSTANCE;
  }

  /**
   * Gets the local date argument
   * @return Local date argument
   */
  public static LocalDateArgument localDate() {
    return LocalDateArgumentImpl.INSTANCE;
  }

  /**
   * Gets the block argument
   * @return Block argument
   */
  public static BlockArgument block() {
    return BlockArgumentImpl.INSTANCE;
  }

  /**
   * Gets the block filter argument
   * @return Block filter argument
   */
  public static BlockFilterArgument blockFilter() {
    return BlockFilterArgumentImpl.INSTANCE;
  }

  /**
   * Gets an entity selector argument that is allowed to only return 1 entity
   * @return Entity selector argument
   */
  public static EntityArgument entity() {
    return EntityArgumentImpl.ENTITY;
  }

  /**
   * Gets an entity selector from a specified {@code context}
   * @param context Command context
   * @param argument Name of the argument
   * @return Found entity selector
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't an
   *                                  {@link EntitySelector}
   */
  public static EntitySelector getSelector(CommandContext<?> context,
                                           String argument
  ) throws IllegalArgumentException {
    return context.getArgument(argument, EntitySelector.class);
  }

  /**
   * Gets a single entity from parsed a entity selector
   *
   * @param context Command context
   * @param argument Name of the argument
   * @return Found entity
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't an
   *                                  {@link EntitySelector}
   * @throws CommandSyntaxException If no entity was found, or if the command
   *                                source in the provided context isn't
   *                                allowed to access entity selectors
   */
  public static Entity getEntity(CommandContext<CommandSource> context,
                                 String argument
  ) throws CommandSyntaxException, IllegalArgumentException {
    return getSelector(context, argument)
        .findEntity(context.getSource());
  }

  /**
   * Gets an entity selector argument that allows for multiple entities to be
   * returned
   *
   * @return Entity selector argument
   */
  public static EntityArgument entities() {
    return EntityArgumentImpl.ENTITIES;
  }

  /**
   * Gets a list of entities from a parsed entity selector
   *
   * @param context Command context
   * @param argument Name of the argument
   * @return Found entities
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't an
   *                                  {@link EntitySelector}
   * @throws CommandSyntaxException If no entity was found, or if the command
   *                                source in the provided context isn't
   *                                allowed to access entity selectors
   */
  public static List<Entity> getEntities(CommandContext<CommandSource> context,
                                         String argument
  ) throws CommandSyntaxException, IllegalArgumentException {
    return getSelector(context, argument)
        .findEntities(context.getSource());
  }

  /**
   * Gets an entity selector argument that only allows a single player to be
   * returned
   *
   * @return Entity selector argument
   */
  public static EntityArgument player() {
    return EntityArgumentImpl.PLAYER;
  }

  /**
   * Gets a single player from a parsed entity selector
   *
   * @param context Command context
   * @param argument Name of the argument
   * @return Found player
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't an
   *                                  {@link EntitySelector}
   * @throws CommandSyntaxException If no entity was found, or if the command
   *                                source in the provided context isn't
   *                                allowed to access entity selectors
   */
  public static Player getPlayer(CommandContext<CommandSource> context,
                                 String argument
  ) throws CommandSyntaxException, IllegalArgumentException {
    return getSelector(context, argument)
        .findPlayer(context.getSource());
  }

  /**
   * Gets and entity selector argument that is only allowed to return players
   * @return Entity selector argument
   */
  public static EntityArgument players() {
    return EntityArgumentImpl.PLAYERS;
  }

  /**
   * Gets a list of players from a parsed entity selector
   *
   * @param context Command context
   * @param argument Name of the argument
   * @return Found players
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't an
   *                                  {@link EntitySelector}
   * @throws CommandSyntaxException If no entity was found, or if the command
   *                                source in the provided context isn't
   *                                allowed to access entity selectors
   */
  public static List<Player> getPlayers(CommandContext<CommandSource> context,
                                        String argument
  ) throws CommandSyntaxException, IllegalArgumentException {
    return getSelector(context, argument)
        .findPlayers(context.getSource());
  }

  /**
   * Gets the loot table argument
   * @return Loot table argument
   */
  public static LootTableArgument lootTable() {
    return LootTableArgumentImpl.INSTANCE;
  }

  /**
   * Gets the particle argument
   * @return Particle argument
   */
  public static ParticleArgument particle() {
    return ParticleArgumentImpl.INSTANCE;
  }

  /**
   * Gets the UUID argument
   * @return UUID argument
   */
  public static UuidArgument uuid() {
    return UuidArgumentImpl.INSTANCE;
  }

  /**
   * Gets an argument that parses a 3D floating point position
   * @return Position argument
   */
  public static PositionArgument position() {
    return PositionArgumentImpl.POSITION;
  }

  /**
   * Gets an argument that parses a 3D integer position
   * @return Block position argument
   */
  public static PositionArgument blockPosition() {
    return PositionArgumentImpl.BLOCK_POSITION;
  }

  /**
   * Gets an argument that parses a 2D floating point position
   * @return 2D position argument
   */
  public static PositionArgument position2d() {
    return PositionArgumentImpl.POSITION_2D;
  }

  /**
   * Gets an argument that parses a 2D integer position
   * @return 2D block position argument
   */
  public static PositionArgument blockPosition2d() {
    return PositionArgumentImpl.BLOCK_POSITION_2D;
  }

  /**
   * Gets a parsed location.
   * <p>
   * Gets the parsed position from the specified {@code context} and t hen calls
   * {@link ParsedPosition#apply(CommandSource)} with the command source in the
   * context
   *
   * @param context Command context
   * @param argument Name of the argument
   * @return Gotten location
   * @throws IllegalArgumentException If the specified {@code argument} doesn't
   *                                  exist, or if it isn't a
   *                                  {@link ParsedPosition}
   */
  public static Location getLocation(CommandContext<CommandSource> context,
                                     String argument
  ) throws IllegalArgumentException {
    ParsedPosition position
        = context.getArgument(argument, ParsedPosition.class);

    return position.apply(context.getSource());
  }

  /**
   * Delegate for {@link #registry(Registry, String)} with
   * {@link Registry#ENCHANTMENT}
   *
   * @return Created argument type
   */
  public static RegistryArgument<Enchantment> enchantment() {
    return registry(Registry.ENCHANTMENT, "enchantment");
  }

  /**
   * Delegate for {@link #registry(Registry, String)} with
   * {@link Registry#POTION_EFFECT_TYPE}
   *
   * @return Created argument type
   */
  public static RegistryArgument<PotionEffectType> potionType() {
    return registry(Registry.POTION_EFFECT_TYPE, "potion_effect");
  }

  /**
   * Creates a registry argument with the specified backing {@code registry} and
   * specified exception factory
   *
   * @param registry Backing registry
   * @param factory  Exception factory used to create exceptions for
   *                 unknown elements
   * @return Created argument type
   * @param <T> Registry type
   */
  public static <T extends Keyed> RegistryArgument<T> registry(
      Registry<T> registry,
      UnknownFactory factory
  ) {
    return new RegistryArgumentImpl<>(registry, factory);
  }

  /**
   * Creates a registry argument with the specified backing {@code registry} and
   * specified {@code name}
   * <p>
   * Uses {@link net.forthecrown.grenadier.ExceptionProvider#unknownResource(NamespacedKey, String, StringReader)}
   * to create the error messages
   *
   * @param registry Backing registry
   * @param name Registry name
   * @return Created argument type
   * @param <T> Registry type
   * @see #registry(Registry, UnknownFactory)
   */
  public static <T extends Keyed> RegistryArgument<T> registry(
      Registry<T> registry,
      String name
  ) {
    return registry(
        registry,
        (reader, key) -> {
          return Grenadier.exceptions().unknownResource(key, name, reader);
        }
    );
  }

  /**
   * Creates an enum argument for the specified {@code enumType}
   * @param enumType Enum class that will be parsed
   * @return Created argument type
   * @param <E> Enum type
   */
  public static <E extends Enum<E>> EnumArgument<E> enumType(Class<E> enumType) {
    return new EnumArgumentImpl<>(enumType);
  }

  /**
   * Creates a map-based argument type with the specified backing map.
   * <p>
   * Note: no key in the specified {@code values} map may feature a whitespace
   * character, as whitespace characters are used to detect when to stop reading
   * input
   *
   * @param values Parse values
   * @return Created argument type
   * @param <T> Map values
   * @throws IllegalArgumentException If any key in the backing map contained
   *                                  a whitespace character
   */
  public static <T> MapArgument<T> map(Map<String, T> values)
      throws IllegalArgumentException
  {
    return new MapArgumentImpl<>(values);
  }

  /**
   * Creates an array argument for the specified {@code type}
   * @param type Individual element parser
   * @return Created array argument
   * @param <T> Array type
   */
  public static <T> ArrayArgument<T> array(ArgumentType<T> type) {
    return new ArrayArgumentImpl<>(type);
  }

  /**
   * Maps one argument types results to another type.
   * <p>
   * For example, say we want to map a {@link TimeArgument}'s result from a
   * {@link Duration} to milliseconds, we would this like so: <pre><code>
   * final TimeArgument time = ArgumentTypes.time();
   *
   * final ArgumentType&lt;Long> mapped
   *     = ArgumentTypes.map(time, Duration::toMillis);
   * </code></pre>
   * The mapped type will use the same parser and provide the same suggestions
   * as the specified {@code fromType}
   *
   * @param fromType The base type that will parse and provide suggestions
   * @param translator The function that translates the base's type
   * @return Mapped argument type
   * @param <F> Base type that will be mapped
   * @param <T> Target type
   */
  public static <F, T> ArgumentType<T> map(ArgumentType<F> fromType,
                                           Function<F, T> translator
  ) {
    return ArgumentTypeMapper.mapType(fromType, translator);
  }
}
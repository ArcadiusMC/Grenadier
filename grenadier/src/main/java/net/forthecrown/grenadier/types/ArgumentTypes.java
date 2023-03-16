package net.forthecrown.grenadier.types;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.RegistryArgument.UnknownFactory;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class ArgumentTypes {
  private ArgumentTypes() {}

  public static ItemArgument item() {
    return ItemArgumentImpl.INSTANCE;
  }

  public static ItemFilterArgument itemFilter() {
    return ItemFilterArgumentImpl.INSTANCE;
  }

  public static ComponentArgument component() {
    return ComponentArgumentImpl.INSTANCE;
  }

  public static KeyArgument key() {
    return KeyArgumentImpl.INSTANCE;
  }

  public static WorldArgument world() {
    return WorldArgumentImpl.INSTANCE;
  }

  public static NbtArgument<BinaryTag> binaryTag() {
    return NbtArgumentImpl.BINARY_TAG;
  }

  public static NbtArgument<CompoundTag> compoundTag() {
    return NbtArgumentImpl.COMPOUND;
  }

  public static TagPathArgument tagPath() {
    return TagPathArgumentImpl.INSTANCE;
  }

  public static GameModeArgument gameMode() {
    return GameModeArgumentImpl.INSTANCE;
  }

  public static TimeArgument time() {
    return TimeArgumentImpl.INSTANCE;
  }

  public static Duration getDuration(CommandContext<?> context, String argument) {
    return context.getArgument(argument, Duration.class);
  }

  public static long getMillis(CommandContext<?> context, String argument) {
    return getDuration(context, argument).toMillis();
  }

  public static long getTicks(CommandContext<?> context, String argument) {
    return getMillis(context, argument) / Ticks.SINGLE_TICK_DURATION_MS;
  }

  public static ObjectiveArgument objective() {
    return ObjectiveArgumentImpl.INSTANCE;
  }

  public static TeamArgument team() {
    return TeamArgumentImpl.INSTANCE;
  }

  public static LocalDateArgument localDate() {
    return LocalDateArgumentImpl.INSTANCE;
  }

  public static BlockArgument block() {
    return BlockArgumentImpl.INSTANCE;
  }

  public static BlockFilterArgument blockFilter() {
    return BlockFilterArgumentImpl.INSTANCE;
  }

  public static EntityArgument entity() {
    return EntityArgumentImpl.ENTITY;
  }

  public static EntitySelector getSelector(CommandContext<?> context,
                                           String argument
  ) {
    return context.getArgument(argument, EntitySelector.class);
  }

  public static Entity getEntity(CommandContext<CommandSource> context,
                                 String argument
  ) throws CommandSyntaxException {
    return getSelector(context, argument)
        .findEntity(context.getSource());
  }

  public static EntityArgument entities() {
    return EntityArgumentImpl.ENTITIES;
  }

  public static List<Entity> getEntities(CommandContext<CommandSource> context,
                                         String argument
  ) throws CommandSyntaxException {
    return getSelector(context, argument)
        .findEntities(context.getSource());
  }

  public static EntityArgument player() {
    return EntityArgumentImpl.PLAYER;
  }

  public static Player getPlayer(CommandContext<CommandSource> context,
                                 String argument
  ) throws CommandSyntaxException {
    return getSelector(context, argument)
        .findPlayer(context.getSource());
  }

  public static EntityArgument players() {
    return EntityArgumentImpl.PLAYERS;
  }

  public static List<Player> getPlayers(CommandContext<CommandSource> context,
                                        String argument
  ) throws CommandSyntaxException {
    return getSelector(context, argument)
        .findPlayers(context.getSource());
  }

  public static LootTableArgument lootTable() {
    return LootTableArgumentImpl.INSTANCE;
  }

  public static ParticleArgument particle() {
    return ParticleArgumentImpl.INSTANCE;
  }

  public static UuidArgument uuid() {
    return UuidArgumentImpl.INSTANCE;
  }

  public static RegistryArgument<Enchantment> enchantment() {
    return registry(Registry.ENCHANTMENT, "enchantment");
  }

  public static RegistryArgument<PotionEffectType> potionType() {
    return registry(Registry.POTION_EFFECT_TYPE, "potion_effect");
  }

  public static <T extends Keyed> RegistryArgument<T> registry(
      Registry<T> registry,
      UnknownFactory factory
  ) {
    return new RegistryArgumentImpl<>(registry, factory);
  }

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

  public static <E extends Enum<E>> EnumArgument<E> enumType(Class<E> enumType) {
    return new EnumArgumentImpl<>(enumType);
  }

  public static <T> MapArgument<T> mapArgument(Map<String, T> values) {
    return new MapArgumentImpl<>(values);
  }

  public static <T> ArrayArgument<T> array(ArgumentType<T> type) {
    return new ArrayArgumentImpl<>(type);
  }
}
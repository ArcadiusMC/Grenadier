package net.forthecrown.grenadier.types;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.Map;
import net.forthecrown.grenadier.internal.types.ArgumentTypeProviderImpl;
import net.forthecrown.grenadier.types.RegistryArgument.UnknownFactory;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public final class ArgumentTypes {
  private ArgumentTypes() {}

  private static final ArgumentTypeProvider provider
      = new ArgumentTypeProviderImpl();

  public static ArgumentTypeProvider typeProvider() {
    return provider;
  }

  public static ItemArgument item() {
    return typeProvider().getItemArgument();
  }

  public static ItemFilterArgument itemFilter() {
    return typeProvider().getItemFilterArgument();
  }

  public static ComponentArgument component() {
    return typeProvider().getComponentArgument();
  }

  public static <E extends Enum<E>> EnumArgument<E> enumType(Class<E> enumType) {
    return typeProvider().createEnumArgument(enumType);
  }

  public static <T> MapArgument<T> mapArgument(Map<String, T> values) {
    return typeProvider().createMapArgument(values);
  }

  public static <T> ArrayArgument<T> array(ArgumentType<T> type) {
    return typeProvider().createArrayArgument(type);
  }

  public static KeyArgument key() {
    return typeProvider().getKeyArgument();
  }

  public static WorldArgument world() {
    return typeProvider().getWorldArgument();
  }

  public static RegistryArgument<Enchantment> enchantment() {
    return typeProvider().getEnchantmentArgument();
  }

  public static RegistryArgument<PotionEffectType> potionType() {
    return typeProvider().getPotionTypeArgument();
  }

  public static NbtArgument<BinaryTag> binaryTag() {
    return typeProvider().getTagArgument();
  }

  public static NbtArgument<CompoundTag> compoundTag() {
    return typeProvider().getCompoundTagArgument();
  }

  public static TagPathArgument tagPath() {
    return typeProvider().getTagPathArgument();
  }

  public static GameModeArgument gameMode() {
    return typeProvider().getGameModeArgument();
  }

  public static TimeArgument time() {
    return typeProvider().getTimeArgument();
  }

  public static ObjectiveArgument objective() {
    return typeProvider().getObjectiveArgument();
  }

  public static TeamArgument team() {
    return typeProvider().getTeamArgument();
  }

  public static <T extends Keyed> RegistryArgument<T> registry(
      Registry<T> registry,
      UnknownFactory factory
  ) {
    return typeProvider().createRegistryArgument(registry, factory);
  }

  public static <T extends Keyed> RegistryArgument<T> registry(
      Registry<T> registry,
      String name
  ) {
    return typeProvider().createRegistryArgument(registry, name);
  }
}
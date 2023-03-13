package net.forthecrown.grenadier.types;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.Map;
import net.forthecrown.grenadier.types.RegistryArgument.UnknownFactory;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public interface ArgumentTypeProvider {
  ItemArgument getItemArgument();

  ItemFilterArgument getItemFilterArgument();

  <E extends Enum<E>> EnumArgument<E> createEnumArgument(Class<E> type);

  <T> MapArgument<T> createMapArgument(Map<String, T> values);

  ComponentArgument getComponentArgument();

  <T> ArrayArgument<T> createArrayArgument(ArgumentType<T> type);

  KeyArgument getKeyArgument();

  WorldArgument getWorldArgument();

  RegistryArgument<Enchantment> getEnchantmentArgument();

  RegistryArgument<PotionEffectType> getPotionTypeArgument();

  NbtArgument<BinaryTag> getTagArgument();

  NbtArgument<CompoundTag> getCompoundTagArgument();

  TagPathArgument getTagPathArgument();

  GameModeArgument getGameModeArgument();

  TimeArgument getTimeArgument();

  ObjectiveArgument getObjectiveArgument();

  TeamArgument getTeamArgument();

  <T extends Keyed> RegistryArgument<T> createRegistryArgument(
      Registry<T> registry,
      UnknownFactory factory
  );

  <T extends Keyed> RegistryArgument<T> createRegistryArgument(
      Registry<T> registry,
      String registryName
  );
}
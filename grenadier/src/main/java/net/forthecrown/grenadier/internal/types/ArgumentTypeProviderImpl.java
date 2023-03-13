package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.ArgumentTypeProvider;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.MapArgument;
import net.forthecrown.grenadier.types.RegistryArgument;
import net.forthecrown.grenadier.types.RegistryArgument.UnknownFactory;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.string.Snbt;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

@Getter
public class ArgumentTypeProviderImpl implements ArgumentTypeProvider {

  private final WorldArgumentImpl worldArgument = new WorldArgumentImpl();

  private final ItemFilterArgumentImpl itemFilterArgument
      = new ItemFilterArgumentImpl();

  private final ItemArgumentImpl itemArgument = new ItemArgumentImpl();

  private final ComponentArgumentImpl componentArgument
      = new ComponentArgumentImpl();

  private final KeyArgumentImpl keyArgument = new KeyArgumentImpl();

  private final RegistryArgument<Enchantment> enchantmentArgument
      = createRegistryArgument(Registry.ENCHANTMENT, "enchantment");

  private final RegistryArgument<PotionEffectType> potionTypeArgument
      = createRegistryArgument(Registry.POTION_EFFECT_TYPE, "potion");

  private final TagPathArgumentImpl tagPathArgument
      = new TagPathArgumentImpl();

  private final NbtArgumentImpl<BinaryTag> tagArgument
      = new NbtArgumentImpl<>(Snbt::parse);

  private final NbtArgumentImpl<CompoundTag> compoundTagArgument
      = new NbtArgumentImpl<>(Snbt::parseCompound);

  private final GameModeArgumentImpl gameModeArgument
      = new GameModeArgumentImpl();

  private final TimeArgumentImpl timeArgument
      = new TimeArgumentImpl();

  private final ObjectiveArgumentImpl objectiveArgument
      = new ObjectiveArgumentImpl();

  private final TeamArgumentImpl teamArgument
      = new TeamArgumentImpl();

  private final Map<Class<?>, EnumArgumentImpl> enumArguments
      = new HashMap<>();

  @Override
  public <E extends Enum<E>> EnumArgument<E> createEnumArgument(Class<E> type) {
    return enumArguments.computeIfAbsent(
        type,
        aClass -> new EnumArgumentImpl(aClass)
    );
  }

  @Override
  public <T> MapArgument<T> createMapArgument(Map<String, T> values) {
    return new MapArgumentImpl<>(values);
  }

  @Override
  public <T> ArrayArgument<T> createArrayArgument(ArgumentType<T> type) {
    return new ArrayArgumentImpl<>(type);
  }

  @Override
  public <T extends Keyed> RegistryArgument<T> createRegistryArgument(
      Registry<T> registry,
      UnknownFactory factory
  ) {
    return new RegistryArgumentImpl<>(registry, factory);
  }

  @Override
  public <T extends Keyed> RegistryArgument<T> createRegistryArgument(
      Registry<T> registry,
      String registryName
  ) {
    return createRegistryArgument(
        registry,
        (reader, key) -> Grenadier.exceptions()
            .unknownResource(key, registryName, reader)
    );
  }
}
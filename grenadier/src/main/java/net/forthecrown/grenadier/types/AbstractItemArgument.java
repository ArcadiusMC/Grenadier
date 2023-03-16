package net.forthecrown.grenadier.types;

import net.forthecrown.grenadier.internal.InternalUtil;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

abstract class AbstractItemArgument implements VanillaMappedArgument {
  protected final HolderLookup<Item> holderLookup
      = InternalUtil.lookup(Registries.ITEM);

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}
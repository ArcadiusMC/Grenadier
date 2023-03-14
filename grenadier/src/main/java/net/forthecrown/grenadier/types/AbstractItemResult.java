package net.forthecrown.grenadier.types;

import net.forthecrown.grenadier.internal.InternalUtil;
import net.forthecrown.nbt.CompoundTag;

abstract class AbstractItemResult {

  protected final CompoundTag tag;

  public AbstractItemResult(net.minecraft.nbt.CompoundTag vanillaTag) {
    this.tag = InternalUtil.fromVanillaTag(vanillaTag);
  }

  public CompoundTag getTag() {
    return tag == null ? null : tag.copy();
  }
}
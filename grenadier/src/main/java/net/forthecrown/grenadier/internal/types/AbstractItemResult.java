package net.forthecrown.grenadier.internal.types;

import lombok.Getter;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.TagTranslators;

@Getter
public abstract class AbstractItemResult {
  final CompoundTag tag;

  public AbstractItemResult(net.minecraft.nbt.CompoundTag vanillaTag) {
    this.tag = vanillaTag == null
        ? null
        : TagTranslators.COMPOUND.toApiType(vanillaTag);
  }
}
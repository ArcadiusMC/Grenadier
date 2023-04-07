package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface VariableHolder extends Tree {
  String variable();
}
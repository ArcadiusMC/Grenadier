package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface Tree {

  int tokenStart();

  <R, C> R accept(TreeVisitor<R, C> visitor, C context);
}
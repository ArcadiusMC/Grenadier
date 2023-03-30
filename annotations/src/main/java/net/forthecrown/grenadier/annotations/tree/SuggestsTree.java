package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface SuggestsTree extends Tree {

  record StringListSuggestions(String[] suggestions) implements SuggestsTree {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitStringSuggestions(this, context);
    }
  }

  record ComponentRefSuggestions(ClassComponentRef ref)
      implements SuggestsTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitRefSuggestions(this, context);
    }
  }

  record VariableSuggestions(String variable)
      implements SuggestsTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableSuggests(this, context);
    }
  }
}
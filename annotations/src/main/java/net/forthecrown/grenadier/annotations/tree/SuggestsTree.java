package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface SuggestsTree extends Tree {

  record StringListSuggestions(int tokenStart, String[] suggestions) implements SuggestsTree {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitStringSuggestions(this, context);
    }
  }

  record ComponentRefSuggestions(int tokenStart, MemberChainTree ref)
      implements SuggestsTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitRefSuggestions(this, context);
    }
  }

  record VariableSuggestions(int tokenStart, String variable)
      implements SuggestsTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableSuggests(this, context);
    }
  }
}
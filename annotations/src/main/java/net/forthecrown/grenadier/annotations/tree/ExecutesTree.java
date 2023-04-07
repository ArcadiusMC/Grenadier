package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ExecutesTree extends Tree {

  record MemberExecutes(int tokenStart, MemberChainTree ref) implements ExecutesTree {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitMemberExecutes(this, context);
    }
  }

  record VariableExecutes(int tokenStart, String variable)
      implements ExecutesTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableExecutes(this, context);
    }
  }
}
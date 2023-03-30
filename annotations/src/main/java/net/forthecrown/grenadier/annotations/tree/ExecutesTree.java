package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface ExecutesTree extends Tree {

  record RefExecution(ClassComponentRef ref) implements ExecutesTree {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitRefExec(this, context);
    }
  }

  record VariableExecutes(String variable)
      implements ExecutesTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVarExec(this, context);
    }
  }
}
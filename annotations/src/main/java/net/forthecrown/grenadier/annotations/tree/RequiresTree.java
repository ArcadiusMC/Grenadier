package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface RequiresTree extends Tree {

  record RequiresRef(ClassComponentRef ref) implements RequiresTree {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitRefRequires(this, context);
    }
  }

  record PermissionRequires(Name name) implements RequiresTree {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitPermissionRequirement(this, context);
    }
  }

  record VariableRequires(String variable)
      implements RequiresTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableRequires(this, context);
    }
  }
}
package net.forthecrown.grenadier.annotations.tree;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface Name extends Tree {

  @Internal
  record DirectName(String value) implements Name {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitDirectName(this, context);
    }
  }

  @Internal
  record VariableName(String variable) implements Name, VariableHolder {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableName(this, context);
    }
  }
}
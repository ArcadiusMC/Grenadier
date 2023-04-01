package net.forthecrown.grenadier.annotations.tree;

public interface ArgumentMapperTree extends Tree {

  Name argumentName();

  record RefMapper(Name argumentName, ClassComponentRef ref)
      implements ArgumentMapperTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitRefModifier(this, context);
    }
  }

  record VariableMapper(Name argumentName, String variable)
      implements ArgumentMapperTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVarModifier(this, context);
    }
  }

  record InvokeResultMethod(Name argumentName, ClassComponentRef ref)
      implements ArgumentMapperTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitResultInvokeModifier(this, context);
    }
  }
}
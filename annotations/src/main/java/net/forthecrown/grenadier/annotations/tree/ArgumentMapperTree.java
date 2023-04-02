package net.forthecrown.grenadier.annotations.tree;

public interface ArgumentMapperTree extends Tree {

  Name argumentName();

  record RefMapper(int tokenStart, Name argumentName, ClassComponentRef ref)
      implements ArgumentMapperTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitRefModifier(this, context);
    }
  }

  record VariableMapper(int tokenStart, Name argumentName, String variable)
      implements ArgumentMapperTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVarModifier(this, context);
    }
  }

  record InvokeResultMethod(int tokenStart, Name argumentName, ClassComponentRef ref)
      implements ArgumentMapperTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitResultInvokeModifier(this, context);
    }
  }
}
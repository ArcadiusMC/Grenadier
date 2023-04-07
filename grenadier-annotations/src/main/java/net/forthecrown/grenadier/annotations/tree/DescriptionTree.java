package net.forthecrown.grenadier.annotations.tree;

public interface DescriptionTree extends Tree {

  record LiteralDescription(int tokenStart, String value)
      implements DescriptionTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitLiteralDescription(this, context);
    }
  }

  record VariableDescription(int tokenStart, String variable)
      implements DescriptionTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableDescription(this, context);
    }
  }

  record TranslatableDescription(int tokenStart, String translationKey)
      implements DescriptionTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitTranslatableDescription(this, context);
    }
  }

  record ArrayDescription(int tokenStart, DescriptionTree[] elements)
      implements DescriptionTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitArrayDescription(this, context);
    }
  }
}
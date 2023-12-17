package net.forthecrown.grenadier.annotations.tree;

public interface TransformTree extends Tree {

  record VariableTransform(String variable, int tokenStart)
      implements TransformTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableTransform(this, context);
    }
  }

  record MemberTransform(MemberChainTree tree, int tokenStart) implements TransformTree {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitMemberTransform(this, context);
    }
  }
}

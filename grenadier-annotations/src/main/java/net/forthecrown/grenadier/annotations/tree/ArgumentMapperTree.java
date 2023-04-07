package net.forthecrown.grenadier.annotations.tree;

public interface ArgumentMapperTree extends Tree {

  Name argumentName();

  record MemberMapper(int tokenStart, Name argumentName, MemberChainTree ref)
      implements ArgumentMapperTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitMemberMapper(this, context);
    }
  }

  record VariableMapper(int tokenStart, Name argumentName, String variable)
      implements ArgumentMapperTree, VariableHolder
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitVariableMapper(this, context);
    }
  }

  record ResultMemberMapper(int tokenStart, Name argumentName, MemberChainTree ref)
      implements ArgumentMapperTree
  {

    @Override
    public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
      return visitor.visitResultMemberMapper(this, context);
    }
  }
}
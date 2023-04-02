package net.forthecrown.grenadier.annotations.tree;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Getter @Setter
public class ArgumentTree extends ChildCommandTree {

  private ArgumentTypeRef typeInfo;

  private SuggestsTree suggests;

  @Override
  public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
    return visitor.visitArgument(this, context);
  }
}
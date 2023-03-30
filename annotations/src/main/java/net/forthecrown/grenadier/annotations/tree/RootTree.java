package net.forthecrown.grenadier.annotations.tree;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Getter @Setter
public class RootTree extends AbstractCmdTree {

  private Name permission;
  private String description;

  private List<Name> aliases;

  @Override
  public <R, C> R accept(TreeVisitor<R, C> visitor, C context) {
    return visitor.visitRoot(this, context);
  }
}
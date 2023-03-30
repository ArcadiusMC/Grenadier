package net.forthecrown.grenadier.annotations.tree;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Getter @Setter
public abstract class AbstractCmdTree implements Tree {

  private Name name;

  private ExecutesTree executes;

  private RequiresTree requires;

  private final List<ChildCommandTree> children = new ArrayList<>();
}
package net.forthecrown.grenadier.annotations.tree;

import static net.forthecrown.grenadier.annotations.util.Result.NO_POSITION;

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

  private final List<TransformTree> transforms = new ArrayList<>();

  private final List<ChildCommandTree> children = new ArrayList<>();

  private final List<ArgumentMapperTree> mappers = new ArrayList<>();

  private int tokenStart = NO_POSITION;

  private DescriptionTree description;

  private Name syntaxLabel;

  @Override
  public int tokenStart() {
    return tokenStart;
  }
}
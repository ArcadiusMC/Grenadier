package net.forthecrown.grenadier.annotations.tree;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Getter @Setter
public abstract class ChildCommandTree extends AbstractCmdTree {
}
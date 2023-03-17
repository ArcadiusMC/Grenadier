package net.forthecrown.grenadier.types.options;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

abstract class AbstractOption implements Option {

  @Getter
  private final Predicate<CommandSource> condition;

  @Getter
  private final Set<String> labels;

  @Getter
  private final Component tooltip;

  public AbstractOption(AbstractBuilder<?> builder) {
    this.labels = ImmutableSet.copyOf(builder.labels);
    this.condition = builder.condition;
    this.tooltip = builder.tooltip;

    Preconditions.checkArgument(labels.size() > 0, "No labels set");
  }

  @Getter
  static abstract class AbstractBuilder<T extends OptionBuilder<T>>
      implements OptionBuilder<T>
  {
    private final Set<String> labels = new HashSet<>();

    private Component tooltip;

    private Predicate<CommandSource> condition = source -> true;

    @Override
    public T addLabel(String... labels) throws IllegalArgumentException {
      for (var s: labels) {
        Options.validateLabel(s);
        this.labels.add(s);
      }

      return (T) this;
    }

    @Override
    public T setLabels(String... labels) throws IllegalArgumentException {
      this.labels.clear();

      for (var s: labels) {
        Options.validateLabel(s);
        this.labels.add(s);
      }

      return (T) this;
    }

    public T setCondition(@NotNull Predicate<CommandSource> condition) {
      this.condition = Objects.requireNonNull(condition);
      return (T) this;
    }

    public T setTooltip(Component tooltip) {
      this.tooltip = tooltip;
      return (T) this;
    }
  }

}
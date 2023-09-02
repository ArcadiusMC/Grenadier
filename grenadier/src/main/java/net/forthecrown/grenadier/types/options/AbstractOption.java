package net.forthecrown.grenadier.types.options;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

abstract class AbstractOption implements Option {

  @Getter
  private final Predicate<CommandSource> condition;

  @Getter
  private final String label;

  @Getter
  private final Component tooltip;

  public AbstractOption(AbstractBuilder<?> builder) {
    this.label = builder.label;
    this.condition = builder.condition;
    this.tooltip = builder.tooltip;

    Preconditions.checkArgument(
        !Strings.isNullOrEmpty(label),
        "Argument label is null or empty"
    );
  }

  @Getter
  static abstract class AbstractBuilder<T extends OptionBuilder<T>>
      implements OptionBuilder<T>
  {
    private String label = null;

    private Component tooltip;

    private Predicate<CommandSource> condition = source -> true;

    @Override
    public T setLabel(String label) throws IllegalArgumentException {
      Objects.requireNonNull(label, "Null label");
      Options.validateLabel(label);

      this.label = label;
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
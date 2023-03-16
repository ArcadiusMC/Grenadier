package net.forthecrown.grenadier.types.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;

@Getter
public final class FlagOption implements Option {

  private final List<String> labels;
  private final Predicate<CommandSource> condition;
  private final Component tooltip;

  public FlagOption(Builder builder) {
    this.labels = List.copyOf(builder.getLabels());
    this.condition = Objects.requireNonNull(builder.getCondition());
    this.tooltip = builder.getTooltip();

    Option.validateLabels(labels);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Builder implements OptionBuilder<Builder> {
    private final List<String> labels = new ArrayList<>();

    private Predicate<CommandSource> condition = source -> true;

    private Component tooltip;

    public FlagOption build() {
      return new FlagOption(this);
    }
  }
}
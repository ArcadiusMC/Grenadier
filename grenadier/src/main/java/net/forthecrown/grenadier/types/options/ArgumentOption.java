package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.arguments.ArgumentType;
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
public final class ArgumentOption<T> implements Option {
  private final List<String> labels;

  private final ArgumentType<T> argumentType;

  private final T defaultValue;

  private final Predicate<CommandSource> condition;

  private final Component tooltip;

  public ArgumentOption(Builder<T> builder) {
    this.labels = List.copyOf(builder.getLabels());
    this.argumentType = Objects.requireNonNull(builder.getArgumentType());
    this.defaultValue = builder.getDefaultValue();
    this.condition = Objects.requireNonNull(builder.getCondition());
    this.tooltip = builder.getTooltip();

    Option.validateLabels(labels);
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static <T> Builder<T> builder(ArgumentType<T> type) {
    return ArgumentOption.<T>builder().setArgumentType(type);
  }

  public static <T> ArgumentOption<T> of(String name, ArgumentType<T> type) {
    return builder(type).addLabel(name).build();
  }

  @Getter
  @Setter
  @Accessors(chain = true)
  public static class Builder<T> implements OptionBuilder<Builder<T>> {

    private final List<String> labels = new ArrayList<>();

    private T defaultValue;

    private ArgumentType<T> argumentType;

    private Predicate<CommandSource> condition = source -> true;

    private Component tooltip;

    public ArgumentOption<T> build() {
      return new ArgumentOption<>(this);
    }
  }
}
package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.arguments.ArgumentType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Suggester;

@Getter
class ArgumentOptionImpl<T>
    extends AbstractOption
    implements ArgumentOption<T>
{

  private final ArgumentType<T> argumentType;
  private final T defaultValue;

  private final Suggester<CommandSource> suggester;

  private final Set<ArgumentOption<?>> required;
  private final Set<ArgumentOption<?>> exclusive;

  public ArgumentOptionImpl(BuilderImpl<T> builder) {
    super(builder);

    this.argumentType = builder.type;
    this.defaultValue = builder.defaultValue;
    this.suggester = builder.suggester;
    this.required = builder.required;
    this.exclusive = builder.exclusive;

    exclusive.forEach(o -> {
      ArgumentOptionImpl<?> impl = (ArgumentOptionImpl<?>) o;
      impl.exclusive.add(this);
    });
  }

  public Set<ArgumentOption<?>> getRequired() {
    return Collections.unmodifiableSet(required);
  }

  @Override
  public Set<ArgumentOption<?>> getMutuallyExclusive() {
    return Collections.unmodifiableSet(exclusive);
  }

  @Setter
  @Getter
  @Accessors(chain = true)
  static class BuilderImpl<T>
      extends AbstractBuilder<Builder<T>>
      implements Builder<T>
  {

    private final ArgumentType<T> type;

    private T defaultValue;

    private Suggester<CommandSource> suggester;

    private final Set<ArgumentOption<?>> required = new HashSet<>();
    private final Set<ArgumentOption<?>> exclusive = new HashSet<>();

    public BuilderImpl(ArgumentType<T> type) {
      this.type = type;
    }

    @Override
    public Builder<T> requires(ArgumentOption<?> other) {
      required.add(other);
      return this;
    }

    @Override
    public Builder<T> mutuallyExclusiveWith(ArgumentOption<?> option) {
      exclusive.add(option);
      return this;
    }

    @Override
    public ArgumentOption<T> build() {
      return new ArgumentOptionImpl<>(this);
    }
  }
}
package net.forthecrown.grenadier.types.options;

import com.mojang.brigadier.arguments.ArgumentType;
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

  public ArgumentOptionImpl(BuilderImpl<T> builder) {
    super(builder);

    this.argumentType = builder.type;
    this.defaultValue = builder.defaultValue;
    this.suggester = builder.suggester;
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

    public BuilderImpl(ArgumentType<T> type) {
      this.type = type;
    }

    @Override
    public ArgumentOption<T> build() {
      return new ArgumentOptionImpl<>(this);
    }
  }
}
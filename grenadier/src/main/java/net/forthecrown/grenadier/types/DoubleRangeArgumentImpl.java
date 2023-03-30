package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.OptionalDouble;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.NumberRanges.NumericRange;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.RangeArgument;
import org.jetbrains.annotations.NotNull;

class DoubleRangeArgumentImpl
    implements DoubleRangeArgument, VanillaMappedArgument
{

  static DoubleRangeArgument DOUBLE_RANGE = new DoubleRangeArgumentImpl();

  @Override
  public DoubleRange parse(StringReader reader) throws CommandSyntaxException {
    return NumberRanges.parseDoubles(reader);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return RangeArgument.floatRange();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }

  static class DoubleRangeImpl
      extends NumericRange<Double>
      implements DoubleRange
  {

    static final DoubleRangeImpl UNLIMITED = new DoubleRangeImpl(null, null);

    public DoubleRangeImpl(Double min, Double max) {
      super(min, max);
    }

    @Override
    public @NotNull OptionalDouble min() {
      return min == null
          ? OptionalDouble.empty()
          : OptionalDouble.of(min);
    }

    @Override
    public @NotNull OptionalDouble max() {
      return max == null
          ? OptionalDouble.empty()
          : OptionalDouble.of(max);
    }

    @Override
    public boolean contains(double value) {
      return (min == null || value >= min)
          && (max == null || value <= max);
    }
  }
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.OptionalInt;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.NumberRanges.NumericRange;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.RangeArgument;
import org.jetbrains.annotations.NotNull;

class IntRangeArgumentImpl implements IntRangeArgument, VanillaMappedArgument {

  static final IntRangeArgument INT_RANGE = new IntRangeArgumentImpl();

  @Override
  public IntRange parse(StringReader reader) throws CommandSyntaxException {
    return NumberRanges.parseInts(reader);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return RangeArgument.intRange();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }

  static class IntRangeImpl
      extends NumericRange<Integer>
      implements IntRange
  {

    static final IntRangeImpl UNLIMITED = new IntRangeImpl(null, null);

    public IntRangeImpl(Integer min, Integer max) {
      super(min, max);
    }

    @Override
    public @NotNull OptionalInt min() {
      return min == null
          ? OptionalInt.empty()
          : OptionalInt.of(min);
    }

    @Override
    public @NotNull OptionalInt max() {
      return max == null
          ? OptionalInt.empty()
          : OptionalInt.of(max);
    }

    @Override
    public boolean contains(int value) {
      return (min == null || value >= min)
          && (max == null || value <= max);
    }
  }
}
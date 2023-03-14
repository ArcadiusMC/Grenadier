package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.function.Function;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.internal.StringReaderWrapper;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.string.Snbt;
import net.forthecrown.nbt.string.TagParseException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.NbtTagArgument;

class NbtArgumentImpl<T extends BinaryTag>
    implements NbtArgument<T>, VanillaMappedArgument
{

  static final NbtArgument<CompoundTag> COMPOUND
      = new NbtArgumentImpl<>(Snbt::parseCompound);

  static final NbtArgument<BinaryTag> BINARY_TAG
      = new NbtArgumentImpl<>(Snbt::parse);

  private final Function<StringReaderWrapper, T> function;

  public NbtArgumentImpl(Function<StringReaderWrapper, T> function) {
    this.function = Objects.requireNonNull(function);
  }

  @Override
  public T parse(StringReader reader) throws CommandSyntaxException {
    try {
      return function.apply(new StringReaderWrapper(reader));
    } catch (TagParseException exc) {
      reader.setCursor(exc.getParseOffset());

      throw Grenadier.exceptions()
          .tagParseException(exc, reader);
    }
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return NbtTagArgument.nbtTag();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}
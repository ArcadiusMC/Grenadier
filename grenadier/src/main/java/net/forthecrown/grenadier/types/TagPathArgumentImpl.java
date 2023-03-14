package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.internal.StringReaderWrapper;
import net.forthecrown.nbt.path.PathParseException;
import net.forthecrown.nbt.path.TagPath;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.NbtPathArgument;

class TagPathArgumentImpl
    implements TagPathArgument, VanillaMappedArgument
{

  static final TagPathArgument INSTANCE = new TagPathArgumentImpl();

  @Override
  public TagPath parse(StringReader reader) throws CommandSyntaxException {
    StringReaderWrapper wrapper = new StringReaderWrapper(reader);

    try {
      return TagPath.parse(wrapper);
    } catch (PathParseException exc) {
      reader.setCursor(exc.getPosition());
      throw Grenadier.exceptions()
          .pathParseException(exc, reader);
    }
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return NbtPathArgument.nbtPath();
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }
}
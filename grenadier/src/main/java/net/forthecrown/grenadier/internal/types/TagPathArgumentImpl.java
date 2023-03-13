package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.TagPathArgument;
import net.forthecrown.grenadier.utils.StringReaderWrapper;
import net.forthecrown.nbt.path.PathParseException;
import net.forthecrown.nbt.path.TagPath;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.NbtPathArgument;

public class TagPathArgumentImpl
    implements TagPathArgument, VanillaMappedArgument
{

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
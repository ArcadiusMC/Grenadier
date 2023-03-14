package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;

class UuidArgumentImpl implements UuidArgument, VanillaMappedArgument {

  static final UuidArgument INSTANCE = new UuidArgumentImpl();

  @Override
  public UUID parse(StringReader reader) throws CommandSyntaxException {
    return net.minecraft.commands.arguments.UuidArgument.uuid().parse(reader);
  }

  @Override
  public boolean useVanillaSuggestions() {
    return true;
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return net.minecraft.commands.arguments.UuidArgument.uuid();
  }
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;

public interface UuidArgument extends ArgumentType<UUID> {

  @Override
  UUID parse(StringReader reader) throws CommandSyntaxException;
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.nbt.BinaryTag;

public interface NbtArgument<T extends BinaryTag> extends ArgumentType<T> {

  @Override
  T parse(StringReader reader) throws CommandSyntaxException;
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.nbt.path.TagPath;

public interface TagPathArgument extends ArgumentType<TagPath> {

  @Override
  TagPath parse(StringReader reader) throws CommandSyntaxException;
}
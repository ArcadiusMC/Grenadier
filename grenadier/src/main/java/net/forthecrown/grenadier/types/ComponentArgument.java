package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;

public interface ComponentArgument extends ArgumentType<Component> {

  @Override
  Component parse(StringReader reader) throws CommandSyntaxException;
}
package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;

/**
 * Parses a chat Component from JSON
 * <p>
 * Input examples: <pre>
 * {"text": "Hello, world!"}
 * {"text": "Hello, world!", "color":"red"}
 * </pre>
 */
public interface ComponentArgument extends ArgumentType<Component> {

  @Override
  Component parse(StringReader reader) throws CommandSyntaxException;
}
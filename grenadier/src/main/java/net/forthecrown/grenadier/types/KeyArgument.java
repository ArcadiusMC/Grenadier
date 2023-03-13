package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.NamespacedKey;

public interface KeyArgument extends ArgumentType<NamespacedKey> {
  @Override
  NamespacedKey parse(StringReader reader) throws CommandSyntaxException;
}
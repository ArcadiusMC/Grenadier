package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import org.bukkit.NamespacedKey;

class KeyArgumentImpl implements KeyArgument, VanillaMappedArgument {

  static final KeyArgument INSTANCE = new KeyArgumentImpl();

  @Override
  public NamespacedKey parse(StringReader reader)
      throws CommandSyntaxException
  {
    final int start = reader.getCursor();
    String name = readKeyString(reader);

    NamespacedKey key;

    try {
      key = NamespacedKey.fromString(name);
    } catch (IllegalArgumentException exc) {
      key = null;
    }

    if (key == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().invalidKey(name, reader);
    }

    return key;
  }

  public static String readKeyString(StringReader reader) {
    StringBuilder builder = new StringBuilder();

    while (reader.canRead() && allowedInKey(reader.peek())) {
      builder.append(reader.read());
    }

    return builder.toString();
  }

  public static boolean allowedInKey(char c) {
    return (c >= '0' && c <= '9')
        || (c >= 'a' && c <= 'z')
        || c == '_'
        || c == ':'
        || c == '/'
        || c == '.'
        || c == '-';
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return ResourceLocationArgument.id();
  }
}
package net.forthecrown.grenadier.internal.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.KeyArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import org.bukkit.NamespacedKey;

public class KeyArgumentImpl implements KeyArgument, VanillaMappedArgument {

  @Override
  public NamespacedKey parse(StringReader reader)
      throws CommandSyntaxException
  {
    final int start = reader.getCursor();
    StringBuilder builder = new StringBuilder();

    while (reader.canRead() && allowedInKey(reader.peek())) {
      builder.append(reader.read());
    }

    String name = builder.toString();

    try {
      return NamespacedKey.fromString(name);
    } catch (IllegalArgumentException exc) {
      reader.setCursor(start);
      throw Grenadier.exceptions().invalidKey(name, reader);
    }
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

  @Override
  public boolean useVanillaSuggestions() {
    return false;
  }
}
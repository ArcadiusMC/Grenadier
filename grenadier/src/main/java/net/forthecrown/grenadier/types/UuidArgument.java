package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.UUID;

/**
 * Parses a hexadecimal UUID
 * <p>
 * Input examples: <pre>
 * 21290ce5-679c-4917-b30e-168c0d450c72
 * </pre>
 */
public interface UuidArgument extends ArgumentType<UUID> {

  @Override
  UUID parse(StringReader reader) throws CommandSyntaxException;
}
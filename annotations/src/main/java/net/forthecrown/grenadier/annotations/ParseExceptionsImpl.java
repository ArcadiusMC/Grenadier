package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.annotations.util.ErrorMessages;

@RequiredArgsConstructor
class ParseExceptionsImpl implements ParseExceptions {
  private final StringReader input;

  @Override
  public CommandParseException create(int pos, String format, Object... args) {
    String message = ErrorMessages.formatError(input, pos, format, args);
    return new CommandParseException(message);
  }
}
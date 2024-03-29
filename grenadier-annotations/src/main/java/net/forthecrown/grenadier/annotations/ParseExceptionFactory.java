package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.annotations.util.ErrorMessages;
import net.forthecrown.grenadier.annotations.util.Result.ErrorExceptionFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

/**
 * Parse exception factory
 */
@RequiredArgsConstructor
public class ParseExceptionFactory
    implements ErrorExceptionFactory<CommandParseException>
{

  /**
   * Parse position value for {@link #create(int, String, Object...)} to
   * indicate there is no input context required
   */
  public static final int NO_POS = -1;
  private final StringReader input;

  public CommandParseException create(int pos, String format, Object... args) {
    String message = ErrorMessages.formatError(input, pos, format, args);
    return new CommandParseException(message);
  }

  @Override
  public CommandParseException createException(int position, String message) {
    return create(position, message);
  }

  public CommandParseException create(String format, Object... args) {
    return create(NO_POS, format, args);
  }

  public String format(int pos, String message) {
    return ErrorMessages.formatError(input, pos, message);
  }

  public CommandParseException wrap(CommandSyntaxException exc) {
    Component msg = Grenadier.fromMessage(exc.getRawMessage());
    Component rendered = GlobalTranslator.render(msg, Locale.ENGLISH);
    String stringMessage = PlainTextComponentSerializer.plainText()
        .serialize(rendered);

    int pos = exc.getCursor();

    return create(pos, stringMessage);
  }
}
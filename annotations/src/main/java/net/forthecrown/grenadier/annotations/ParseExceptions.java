package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.forthecrown.grenadier.Grenadier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

/**
 * {@link CommandParseException} factory
 */
@FunctionalInterface
public interface ParseExceptions {

  /**
   * Parse position value for {@link #create(int, String, Object...)} to
   * indicate there is no input context required
   */
  int NO_POS = -1;

  CommandParseException create(int pos, String format, Object... args);

  default CommandParseException create(String format, Object... args) {
    return create(NO_POS, format, args);
  }

  default CommandParseException wrap(CommandSyntaxException exc) {
    Component msg = Grenadier.fromMessage(exc.getRawMessage());
    Component rendered = GlobalTranslator.render(msg, Locale.ENGLISH);
    String stringMessage = PlainTextComponentSerializer.plainText()
        .serialize(rendered);

    int pos = exc.getCursor();

    return create(pos, stringMessage);
  }

  static ParseExceptions factory(StringReader input) {
    return new ParseExceptionsImpl(input);
  }
}
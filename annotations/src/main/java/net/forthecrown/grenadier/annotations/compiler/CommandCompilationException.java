package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.StringReader;
import java.util.List;
import net.forthecrown.grenadier.annotations.compiler.CompileErrors.Error;
import net.forthecrown.grenadier.annotations.util.ErrorMessages;

public class CommandCompilationException extends RuntimeException {

  public CommandCompilationException(List<Error> errors,
                                     StringReader reader
  ) {
    super(createMessage(errors, reader));
  }

  private static String createMessage(List<Error> errors, StringReader input) {
    StringBuilder builder = new StringBuilder();

    builder.append("Errors during parsing (")
        .append(errors.size())
        .append(" total errors)");

    for (var e: errors) {
      String s = ErrorMessages.formatError(input, e.position(), e.message());

      builder.append("\n");
      builder.append(s);
    }

    return builder.toString();
  }
}
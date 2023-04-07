package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.StringReader;
import java.util.List;
import net.forthecrown.grenadier.annotations.compiler.CompileErrors.Error;
import net.forthecrown.grenadier.annotations.util.ErrorMessages;
import org.jetbrains.annotations.ApiStatus.Internal;

public class CommandCompilationException extends RuntimeException {

  public CommandCompilationException(List<Error> errors,
                                     StringReader reader
  ) {
    super(createMessage(errors, reader));
  }

  @Internal
  public static String createMessage(List<Error> errors, StringReader input) {
    StringBuilder builder = new StringBuilder();

    builder.append("Errors/Warnings during compilation (")
        .append(errors.size())
        .append(" total warnings/errors)");

    for (var e: errors) {
      String s = ErrorMessages.formatError(input, e.position(), e.message());

      builder.append("\n");

      if (e.type() == Error.TYPE_WARN) {
        builder.append("Warning: ");
      } else {
        builder.append("Error: ");
      }

      builder.append(s);
    }

    return builder.toString();
  }
}
package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.StringReader;
import java.io.PrintStream;
import java.util.List;
import net.forthecrown.grenadier.annotations.compiler.CompileErrors.Error;
import net.forthecrown.grenadier.annotations.util.ErrorMessages;
import org.jetbrains.annotations.ApiStatus.Internal;

public class CommandCompilationException extends RuntimeException {

  public CommandCompilationException(List<Error> errors,
                                     StringReader reader,
                                     String name
  ) {
    super(createMessage(errors, reader, name));
  }

  public static void print(List<Error> errors, StringReader input, String name, PrintStream out) {
    String message = createMessage(errors, input, name);
    out.print(message);
    out.print("\n");
  }

  @Internal
  public static String createMessage(List<Error> errors, StringReader input, String name) {
    StringBuilder builder = new StringBuilder();

    builder.append("Errors/Warnings during compilation of command '")
        .append(name)
        .append("' (")
        .append(errors.size())
        .append(" total warnings/errors)");

    for (var e: errors) {
      String s = ErrorMessages.formatError(input, e.position(), e.message());

      builder.append("\n\n");

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
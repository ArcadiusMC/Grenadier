package net.forthecrown.grenadier.annotations.compiler;

import static net.forthecrown.grenadier.annotations.ParseExceptions.NO_POS;
import static net.forthecrown.grenadier.annotations.compiler.CompileErrors.Error.TYPE_ERROR;
import static net.forthecrown.grenadier.annotations.compiler.CompileErrors.Error.TYPE_WARN;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class CompileErrors {

  @Getter
  private final List<Error> errors = new ArrayList<>();

  public void error(String message, Object... args) {
    error(NO_POS, message ,args);
  }

  public void error(int pos, String message, Object... args) {
    Error error = new Error(message.formatted(args), TYPE_ERROR, pos);
    errors.add(error);
  }

  public void warning(String message, Object... args) {
    warning(NO_POS, message, args);
  }

  public void warning(int pos, String message, Object... args) {
    errors.add(new Error(message.formatted(args), TYPE_WARN, pos));
  }

  public int errorCount() {
    return (int) errors.stream()
        .filter(error -> error.type == TYPE_ERROR)
        .count();
  }

  record Error(String message, int type, int position) {
    static final int TYPE_ERROR = 0;
    static final int TYPE_WARN  = 1;
  }
}
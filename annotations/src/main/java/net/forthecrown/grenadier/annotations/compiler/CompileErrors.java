package net.forthecrown.grenadier.annotations.compiler;

import static net.forthecrown.grenadier.annotations.ParseExceptions.NO_POS;

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
    Error error = new Error(message.formatted(args), pos);
    errors.add(error);
  }

  record Error(String message, int position) {
  }
}
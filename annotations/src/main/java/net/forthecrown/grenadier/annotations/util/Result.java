package net.forthecrown.grenadier.annotations.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import net.forthecrown.grenadier.annotations.ParseExceptions;
import net.forthecrown.grenadier.annotations.compiler.CompileErrors;

public class Result<V> {
  public static final int NO_POSITION = -1;

  private final V value;
  private final String error;
  private final int errorPosition;

  private Result(V value, String error, int errorPosition) {
    this.value = value;
    this.error = error;
    this.errorPosition = errorPosition;
  }

  public static <V> Result<V> success(V value) {
    Objects.requireNonNull(value);
    return new Result<>(value, null, NO_POSITION);
  }

  public static <V> Result<V> fail(String message, Object... args) {
    return new Result<>(null, message.formatted(args), NO_POSITION);
  }

  public static <V> Result<V> fail(int position,
                                   String message,
                                   Object... args
  ) {
    return new Result<>(null, message.formatted(args), position);
  }

  public void report(CompileErrors errors) {
    if (error == null) {
      return;
    }

    errors.error(errorPosition, error);
  }

  public V getValue() {
    return value;
  }

  public String getError() {
    return error;
  }

  public int getErrorPosition() {
    return errorPosition;
  }

  public boolean isError() {
    return error != null;
  }

  public Result<V> withPosition(int pos) {
    return pos == this.errorPosition
        ? this
        : new Result<>(value, error, pos);
  }

  public <T> Result<T> map(Function<V, T> mapper) {
    if (value == null || mapper == null) {
      return (Result<T>) this;
    }

    T newValue = mapper.apply(value);
    Objects.requireNonNull(newValue);

    return Result.success(newValue);
  }

  public <T> Result<T> flatMap(Function<V, Result<T>> mapper) {
    if (value == null) {
      return (Result<T>) this;
    }

    var result = mapper.apply(value);
    Objects.requireNonNull(result);

    return result;
  }

  public V orThrow(ParseExceptions exceptions) {
    if (error != null) {
      throw exceptions.create(errorPosition, error);
    }

    return value;
  }

  public V orElse(V value) {
    return error != null ? value : this.value;
  }

  public void consume(Consumer<V> consumer) {
    if (value == null) {
      return;
    }

    consumer.accept(value);
  }

  public void apply(CompileErrors errors, Consumer<V> consumer) {
    if (error != null) {
      errors.error(errorPosition, error);
      return;
    }

    consumer.accept(value);
  }
}
package net.forthecrown.grenadier.annotations.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Container object which may either contain a value or an error message with an
 * optional {@code errorPosition}
 *
 * @param <V> Type of value
 */
public class Result<V> {

  /**
   * {@link #getErrorPosition()} value indicating no value is set
   */
  public static final int NO_POSITION = -1;

  private final V value;
  private final String error;
  private final int errorPosition;

  private Result(V value, String error, int errorPosition) {
    this.value = value;

    this.error = error;
    this.errorPosition = errorPosition;

    // Error missing, value must be present
    if (error == null) {
      Objects.requireNonNull(value, "Both error and value are absent");
    } else {
      if (value != null) {
        throw new IllegalStateException("Both value and error are present");
      }
    }

    // Value missing, error must be present
    if (value == null) {
      Objects.requireNonNull(error, "Both error and value are absent");
    }
  }

  /**
   * Creates a successful result with the specified {@code value}
   * @param value Result value
   * @return Created result
   */
  public static <V> Result<V> success(V value) {
    Objects.requireNonNull(value);
    return new Result<>(value, null, NO_POSITION);
  }

  /**
   * Creates a failure result with a specified message.
   * <p>
   * Uses {@link String#format(String, Object...)} for formatting the message
   * and arguments
   *
   * @param message Message format
   * @param args Message arguments
   * @return Created result
   */
  public static <V> Result<V> fail(String message, Object... args) {
    Objects.requireNonNull(message, "Null message");
    return new Result<>(null, message.formatted(args), NO_POSITION);
  }

  /**
   * Creates a failure result with a specified message.
   * <p>
   * Uses {@link String#format(String, Object...)} for formatting the message
   * and arguments
   *
   * @param position Position with an arbitrary input that this failure occurred
   * @param message Message format
   * @param args Message arguments
   * @return Created result
   */
  public static <V> Result<V> fail(int position, String message, Object... args) {
    Objects.requireNonNull(message, "Null message");
    return new Result<>(null, message.formatted(args), position);
  }

  /**
   * Gets the result's value
   * @return Result value, or {@code null}, if no value is present
   */
  public V getValue() {
    return value;
  }

  /**
   * Gets the result's error message
   * @return Error message, or {@code null}, if the result is a successful
   *         result
   */
  public String getError() {
    return error;
  }

  /**
   * Gets the index this error occurred at in an arbitrary input
   * @return Error position, or {@link #NO_POSITION} if not set
   */
  public int getErrorPosition() {
    return errorPosition;
  }

  /**
   * Tests if this result is an error result.
   * @return {@code true}, if an error is present, {@code false} otherwise
   */
  public boolean isError() {
    return error != null;
  }

  /**
   * Creates a new result with the specified error position
   *
   * @param pos New error position
   * @return Created result
   */
  public Result<V> withPosition(int pos) {
    return pos == this.errorPosition
        ? this
        : new Result<>(value, error, pos);
  }

  /**
   * Maps this result's value to a different type
   * @param mapper Mapping function
   * @return Mapped result, or {@code this}, if it's an error result
   */
  public <T> Result<T> map(Function<V, T> mapper) {
    if (value == null || mapper == null) {
      return (Result<T>) this;
    }

    T newValue = mapper.apply(value);
    Objects.requireNonNull(newValue);

    return Result.success(newValue);
  }

  /**
   * If this is an error result, returns {@code this}, otherwise returns the
   * result of the mapper function
   *
   * @param mapper Mapping function
   * @return {@code this}, if it's an error result, otherwise the value of the
   *         specified {@code mapper} is returned
   */
  public <T> Result<T> flatMap(Function<V, Result<T>> mapper) {
    if (value == null) {
      return (Result<T>) this;
    }

    var result = mapper.apply(value);
    Objects.requireNonNull(result);

    return result;
  }

  /**
   * Reports this result to the specified compile errors. Does nothing if this
   * result is not a failure
   * @param errors Error logger
   */
  public void report(ErrorConsumer errors) {
    Objects.requireNonNull(errors);

    if (error == null) {
      return;
    }

    errors.onError(errorPosition, error);
  }

  /**
   * Gets the result's value, or throws an exception with the specified
   * exception factory
   *
   * @param exceptions Exception factory
   * @return Result value
   */
  public <T extends Throwable> V orThrow(ErrorExceptionFactory<T> exceptions)
      throws T
  {
    if (error != null) {
      throw exceptions.createException(errorPosition, error);
    }

    return value;
  }

  /**
   * Gets the result's value or a default value
   *
   * @param value Default value
   * @return {@link #getValue()} if present, or the {@code value} parameter
   */
  public V orElse(V value) {
    return error != null ? value : this.value;
  }

  /**
   * Either report's this result to the specified {@code errors} logger or calls
   * the specified {@code consumer}, depending on if this is an error result or
   * a successful result
   *
   * @param errors Error reporter
   * @param consumer Value consumer
   */
  public void apply(ErrorConsumer errors, Consumer<V> consumer) {
    if (error != null) {
      errors.onError(errorPosition, error);
      return;
    }

    consumer.accept(value);
  }

  public <T> Result<T> cast() {
    if (!isError()) {
      throw new IllegalStateException("Not an error result");
    }
    return (Result<T>) this;
  }

  /**
   * Maps this result's error message.
   * <p>
   * Calling this function will create a new error result with the mapped message. The error
   * position will also be disregarded
   * <p>
   * If this is NOT an error result, returns {@code this}
   *
   * @param mapper Mapping function
   * @return Created result
   */
  public Result<V> mapError(ErrorMapper mapper) {
    Objects.requireNonNull(mapper, "Null mapper");
    if (!isError()) {
      return this;
    }
    return Result.fail(mapper.mapError(errorPosition, error));
  }

  public Result<V> or(Supplier<Result<V>> supplier) {
    if (!isError()) {
      return this;
    }

    return supplier.get();
  }

  /**
   * Function used to map an error message
   */
  public interface ErrorMapper {

    /**
     * Maps an error message and an error position
     * @param pos Error position
     * @param message Error message
     * @return Mapped message
     */
    String mapError(int pos, String message);
  }

  /**
   * Consumes an error and its position
   */
  public interface ErrorConsumer {

    /**
     * Consumes the specified error message and position
     *
     * @param position Error position, will be {@link #NO_POSITION} if unset
     * @param message Error message
     */
    void onError(int position, String message);
  }

  /**
   * Exception factory
   * @param <T> Exception type
   */
  public interface ErrorExceptionFactory<T extends Throwable> {

    /**
     * Creates an exception from the specified position and error message
     *
     * @param position Error position, will be {@link #NO_POSITION} if unset
     * @param message Error message
     * @return Created exception
     */
    T createException(int position, String message);
  }
}
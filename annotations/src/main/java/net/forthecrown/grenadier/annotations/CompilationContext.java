package net.forthecrown.grenadier.annotations;

import java.util.Map;
import java.util.Optional;

/**
 * Command compilation context.
 * <p>
 * 'Compilation' is a misleading word here, the command is not compiled to
 * bytecode or anything. This is just the context of the tree walker that turns
 * the abstract command syntax tree to an actual command
 *
 * @param typeRegistry Argument type registry
 * @param variables Local and global variables
 * @param loader Class loader
 * @param commandClass Command object
 * @param defaultPermission Default permission format
 * @param exceptions Exception factory
 */
public record CompilationContext(
    TypeRegistry typeRegistry,
    Map<String, Object> variables,
    ClassLoader loader,
    Object commandClass,
    String defaultPermission,
    ParseExceptions exceptions
) {

  /**
   * Gets a variable's optional value
   * @param name Name of the variable
   * @param type Variable's expected type
   * @return Value optional
   * @param <T> Variable's type
   * @throws IllegalStateException If the variable was found, but its type did not
   *                               match the specified {@code type}
   */
  public <T> Optional<T> getVariable(String name, Class<T> type)
      throws IllegalStateException
  {
    Object value = variables.get(name);

    if (value == null) {
      return Optional.empty();
    }

    if (!type.isInstance(value)) {
      throw new IllegalStateException(String.format(
          "Variable '%s' is defined as %s, must be '%s'",
          name, value.getClass().getName(), type.getName()
      ));
    }

    return Optional.of(type.cast(value));
  }

  /**
   * Gets a variable's value, or throws an exception if it's not found
   *
   * @param name Variable's name
   * @param type Variable's expected type
   * @return Variable's value
   * @param <T> Variable's type
   * @throws IllegalStateException If the variable wasn't found, or if the
   *                               variable's type did not match the expected
   *                               type
   */
  public <T> T getOrThrow(String name, Class<T> type)
      throws IllegalStateException
  {
    return getVariable(name, type).orElseThrow(() -> {
      return new IllegalStateException(
          String.format("Variable '%s' not found", name)
      );
    });
  }

  /**
   * Formats the default permission with a command name, if present.
   *
   * @param commandName Command name
   * @return Formatted default permission, or {@code null}, if no default
   *         permission was set
   */
  public String defaultedPermission(String commandName) {
    if (defaultPermission == null) {
      return null;
    }

    return defaultPermission.replace("{command}", commandName);
  }
}
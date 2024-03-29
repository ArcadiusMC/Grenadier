package net.forthecrown.grenadier.annotations;

import java.util.Map;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.annotations.compiler.CommandCompilationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Command registration context
 */
public interface AnnotatedCommandContext {

  /**
   * Creates a new annotated command context, with a null default execution
   * method, null default permission and an empty variable map.
   *
   * @return Created context
   */
  static AnnotatedCommandContext create() {
    return new AnnotatedCommandContextImpl();
  }

  /**
   * Gets a mutable map of variables available to commands registered using
   * this context
   * <p>
   * Any variable placed into this map will throw an {@link IllegalArgumentException}
   * if the variable's key is blank, null or doesn't match the parse specifics
   * of an identifier.
   * <br>
   * See {@link Character#isJavaIdentifierStart(char)} and
   * {@link Character#isJavaIdentifierPart(char)}
   *
   * @return Variable map.
   */
  Map<String, Object> getVariables();

  /**
   * Gets the default permission format used by this context
   * @return Default permission format
   * @see #setDefaultPermissionFormat(String)
   */
  String getDefaultPermissionFormat();

  /**
   * Sets the default permission format used by commands.
   * <p>
   * You can use the {@code {command}} placeholder, which will be replaced by
   * the command's name when a command is registered and no other permission is
   * set
   *
   * @param defaultPermissionFormat Default permission format
   */
  void setDefaultPermissionFormat(String defaultPermissionFormat);

  /**
   * Gets the default executes method of this context
   * @return Default execution method name
   * @see #setDefaultExecutes(String)
   */
  String getDefaultExecutes();

  /**
   * Sets the default execution method.
   * <p>
   * The specified {code defaultExecutes} method will be looked for inside each
   * command that is registered and matches the {@link #getDefaultRule()}
   * conditions.
   * <p>
   * The specified input doesn't go through the same parser as regular input.
   * If the parameter starts with '@' it is considered a variable, else it's
   * considered a method name, field names are not possible from this method.
   *
   * @param defaultExecutes Default executes method name
   */
  void setDefaultExecutes(String defaultExecutes);

  /**
   * Gets the rule that determines when the {@link #getDefaultExecutes()} is
   * placed onto commands
   *
   * @return Default executes rule
   */
  @NotNull DefaultExecutionRule getDefaultRule();

  /**
   * Sets the rule that determines when {@link #getDefaultExecutes()} is placed
   * onto commands.
   *
   * @param defaultRule Default executes rule
   */
  void setDefaultRule(@NotNull DefaultExecutionRule defaultRule);

  /**
   * Determines whether command compilation warnings are printed or not
   *
   * @return {@code true} if command compilation warnings are to be printed,
   *         {@code false}, otherwise
   */
  boolean areWarningsEnabled();

  /**
   * Enables/disables command compilation warnings
   * @param enabled Warnings enabled
   */
  void setWarningsEnabled(boolean enabled);

  /**
   * Tests if errors occurring during command parsing should be thrown as exceptions or logged
   * without distributing the caller of the {@link #registerCommand(Object)} method
   *
   * @return {@code true}, if errors are fatal, {@code false}, otherwise
   */
  boolean fatalErrors();

  /**
   * Sets if errors durring command parsing will be thrown as exceptions or just logged to console
   * @param fatalErrors {@code true}, if errors are fatal, {@code false}, otherwise
   */
  void setFatalErrors(boolean fatalErrors);

  /**
   * Gets the context's type registry
   * @return Type registry
   */
  @NotNull TypeRegistry getTypeRegistry();

  /**
   * Sets the context's type registry
   * @param typeRegistry Argument Type registry
   */
  void setTypeRegistry(@NotNull TypeRegistry typeRegistry);

  /**
   * Loaders are in charge of loading command data annotations that use a
   * 'file = &lt;path&gt;' value
   *
   * @param loader Command data file loader
   */
  void addLoader(CommandDataLoader loader);

  /**
   * Gets the syntax consumer
   * @return Syntax consumer
   * @see #setSyntaxConsumer(SyntaxConsumer)
   */
  SyntaxConsumer getSyntaxConsumer();

  /**
   * Sets the syntax consumer
   * <p>
   * Syntax consumers consume the syntax information created in command trees
   * these can be used in commands like `/help` to provide information on
   * command uses
   *
   * @param consumer Syntax consumer
   */
  void setSyntaxConsumer(@Nullable SyntaxConsumer consumer);

  /**
   * Registers the specified {@code command}
   * @param command Command to register
   * @return Registered command node, or {@code null}, if parsing failed and {@link #fatalErrors()}
   *         was disabled
   *
   * @throws CommandParseException If the command data couldn't be parsed
   * @throws CommandCompilationException If the command couldn't be compiled
   *
   * @see #registerCommand(Object, ClassLoader)
   */
  default GrenadierCommandNode registerCommand(Object command)
      throws CommandParseException, CommandCompilationException
  {
    Class<?> type = command.getClass();
    return registerCommand(command, type.getClassLoader());
  }

  /**
   * Registers the specified {@code command}
   *
   * @param command Command to register
   * @param loader Class loader potentially used by {@link net.forthecrown.grenadier.types.EnumArgument}'s
   *               type parser to find its enum class
   *
   * @return Registered command node, or {@code null}, if parsing failed and {@link #fatalErrors()}
   *         was disabled
   *
   * @throws CommandParseException If the command data couldn't be parsed
   * @throws CommandCompilationException If the command couldn't be compiled
   *
   * @see #registerCommand(Object, ClassLoader)
   */
  GrenadierCommandNode registerCommand(Object command, ClassLoader loader)
      throws CommandParseException, CommandCompilationException;

  /**
   * Defines the rule for the {@link #getDefaultExecutes()} method being used
   * in commands.
   */
  enum DefaultExecutionRule {

    /** Set the default execution if it's missing on the 'root' node */
    IF_MISSING,

    /**
     * Set the default execution if it's misssing on the 'root' node and
     * there's no child nodes
     */
    IF_NO_CHILDREN;
  }
}
package net.forthecrown.grenadier.annotations.compiler;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.SyntaxConsumer;
import net.forthecrown.grenadier.annotations.Token;
import net.forthecrown.grenadier.annotations.TypeRegistry;
import net.forthecrown.grenadier.annotations.tree.VariableHolder;
import net.forthecrown.grenadier.annotations.util.Result;

@Getter
@With
@RequiredArgsConstructor
@AllArgsConstructor
public class CompileContext {

  private final Map<String, Object> variables;
  private final ClassLoader loader;
  private final TypeRegistry typeRegistry;

  private final Object commandClass;
  private final String defaultPermission;

  private final CompileErrors errors;

  private final Stack<String> availableArguments;
  private final Map<String, List<ArgumentModifier>> mappers;

  private SyntaxList syntaxList = new SyntaxList();
  private final List<String> syntaxPrefixes;
  private Stack<Predicate<CommandSource>> predicateStack = new Stack<>();

  public <T> Result<T> getVariable(VariableHolder variable, Class<T> type) {
    return getVariable(variable.tokenStart(), variable.variable(), type);
  }

  public <T> Result<T> getVariable(Token token, Class<T> type) {
    return getVariable(token.position(), token.value(), type);
  }

  /**
   * Gets a variable's optional value
   * @param name Name of the variable
   * @param type Variable's expected type
   * @return Value optional
   * @param <T> Variable's type
   * @throws IllegalStateException If the variable was found, but its type did not
   *                               match the specified {@code type}
   */
  public <T> Result<T> getVariable(int position, String name, Class<T> type)
      throws IllegalStateException
  {
    Object value = variables.get(name);

    if (value == null) {
      return Result.fail(position, "Variable '%s' not found", name);
    }

    if (!type.isInstance(value)) {
      return Result.fail(position,
          "Variable '%s' is defined as %s, must be '%s'",
          name, value.getClass().getName(), type.getName()
      );
    }

    return Result.success((T) value);
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

  public void pushArgument(String argument) {
    availableArguments.push(argument);
  }

  public void popArgument() {
    availableArguments.pop();
  }

  public void pushPrefix(String syntaxPrefix) {
    syntaxPrefixes.add(syntaxPrefix);
  }

  public void popPrefix() {
    syntaxPrefixes.remove(syntaxPrefixes.size() - 1);
  }

  public void pushCondition(Predicate<CommandSource> predicate) {
    predicateStack.push(predicate);
  }

  public void popCondition() {
    predicateStack.pop();
  }

  public Predicate<CommandSource> buildConditions() {
    if (predicateStack.isEmpty()) {
      return null;
    }

    Predicate<CommandSource>[] predicates = new Predicate[predicateStack.size()];
    return new PredicateList(predicateStack.toArray(predicates));
  }

  public CompileContext withModifier(String argumentName,
                                     ArgumentModifier<?, ?> modifier
  ) {
    Map<String, List<ArgumentModifier>> map = new HashMap<>();

    mappers.forEach((s, argumentModifiers) -> {
      map.put(s, new ArrayList<>(argumentModifiers));
    });

    var list = map.computeIfAbsent(argumentName, s -> new ArrayList<>());
    list.add(modifier);

    return withMappers(map);
  }

  public void consumeSyntax(String name, SyntaxConsumer consumer) {
    syntaxList.consume(name, consumer);
  }

  public String syntaxPrefix() {
    return Joiner.on(' ').join(syntaxPrefixes);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public ContextFactory createFactory() {
    return new ContextFactory((Map) mappers);
  }
}
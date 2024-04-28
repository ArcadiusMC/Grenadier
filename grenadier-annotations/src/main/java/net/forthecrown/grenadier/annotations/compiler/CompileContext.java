package net.forthecrown.grenadier.annotations.compiler;

import com.google.common.base.Joiner;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.SyntaxConsumer;
import net.forthecrown.grenadier.annotations.Token;
import net.forthecrown.grenadier.annotations.TypeRegistry;
import net.forthecrown.grenadier.annotations.tree.VariableHolder;
import net.forthecrown.grenadier.annotations.util.PredicateList;
import net.forthecrown.grenadier.annotations.util.Result;
import org.bukkit.plugin.Plugin;

@Getter
@RequiredArgsConstructor
public class CompileContext {

  private final Map<String, Object> variables;
  private final ClassLoader loader;
  private final TypeRegistry typeRegistry;

  private final Object commandClass;
  private final String defaultPermission;

  final CompileErrors errors;

  final SyntaxList syntaxList                      = new SyntaxList();
  final Stack<String> availableArguments           = new Stack<>();
  final Stack<MapperEntry> mappers                 = new Stack<>();
  final Stack<String> syntaxPrefixes               = new Stack<>();
  final Stack<Predicate<CommandSource>> conditions = new Stack<>();

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

  public Predicate<CommandSource> buildConditions() {
    if (conditions.isEmpty()) {
      return null;
    }

    if (conditions.size() == 1) {
      return conditions.get(0);
    }

    Predicate<CommandSource>[] predicates = new Predicate[conditions.size()];
    return new PredicateList(conditions.toArray(predicates));
  }

  public void consumeSyntax(GrenadierCommandNode node, SyntaxConsumer consumer) {
    syntaxList.consume(node, consumer, getCommandClass());
  }

  public String syntaxPrefix() {
    return Joiner.on(' ').join(syntaxPrefixes);
  }

  public ContextFactory createFactory() {
    Map<String, List<ArgumentModifier<?, ?>>> map = new HashMap<>();

    for (MapperEntry mapper : mappers) {
      var list = map.computeIfAbsent(
          mapper.name(), string -> new ArrayList<>()
      );

      list.add(mapper.modifier());
    }

    return new ContextFactory(map);
  }

  public Plugin getPlugin() {
    if (commandClass.getClass().getClassLoader() instanceof ConfiguredPluginClassLoader loader) {
      return loader.getPlugin();
    }
    return null;
  }
}
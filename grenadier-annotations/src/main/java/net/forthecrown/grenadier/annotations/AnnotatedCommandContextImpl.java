package net.forthecrown.grenadier.annotations;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.annotations.compiler.CommandCompilationException;
import net.forthecrown.grenadier.annotations.compiler.CommandCompiler;
import net.forthecrown.grenadier.annotations.compiler.CompileContext;
import net.forthecrown.grenadier.annotations.compiler.CompileErrors;
import net.forthecrown.grenadier.annotations.tree.RootTree;
import net.forthecrown.grenadier.annotations.util.Utils;
import org.jetbrains.annotations.NotNull;

final class AnnotatedCommandContextImpl implements AnnotatedCommandContext {

  @Getter
  private final Map<String, Object> variables = new VariableMap();

  @Getter @Setter
  private String defaultPermissionFormat;

  @Getter @Setter
  private String defaultExecutes;

  @Getter
  private DefaultExecutionRule defaultRule = DefaultExecutionRule.IF_NO_CHILDREN;

  @Getter
  private TypeRegistry typeRegistry = TypeRegistry.global();

  private final List<CommandDataLoader> loaders = new ArrayList<>();

  @Getter @Setter
  private SyntaxConsumer syntaxConsumer;

  @Setter
  private boolean warningsEnabled;

  AnnotatedCommandContextImpl() {
    CommandDataLoader defaultLoader
        = CommandDataLoader.resources(getClass().getClassLoader());

    addLoader(defaultLoader);
  }

  @Override
  public void addLoader(CommandDataLoader loader) {
    Objects.requireNonNull(loader);
    loaders.add(loader);
  }

  @Override
  public void setTypeRegistry(@NotNull TypeRegistry typeRegistry) {
    this.typeRegistry = Objects.requireNonNull(typeRegistry);
  }

  @Override
  public boolean areWarningsEnabled() {
    return warningsEnabled;
  }

  public void setDefaultRule(@NotNull DefaultExecutionRule defaultRule) {
    this.defaultRule = Objects.requireNonNull(defaultRule);
  }

  private StringReader getReader(String value) {
    StringReader reader = new StringReader(value);

    if (!Readers.startsWith(reader, "file")) {
      return reader;
    }

    ParseExceptionFactory exceptions = new ParseExceptionFactory(reader);

    reader.setCursor(reader.getCursor() + "file".length());
    reader.skipWhitespace();

    try {
      reader.expect('=');
    } catch (CommandSyntaxException e) {
      throw exceptions.wrap(e);
    }

    reader.skipWhitespace();

    String path;

    try {
      path = reader.readString();
    } catch (CommandSyntaxException exc) {
      throw exceptions.wrap(exc);
    }

    for (var l: loaders) {
      String inputString;

      try {
        inputString = l.getString(path);
      } catch (IOException exc) {
        exc.printStackTrace(System.err);
        continue;
      }

      if (inputString == null) {
        continue;
      }

      return new StringReader(inputString);
    }

    throw exceptions.create("No valid loader path '%s' found", path);
  }

  @Override
  public GrenadierCommandNode registerCommand(Object command,
                                              ClassLoader loader
  ) throws CommandParseException, CommandCompilationException {
    CommandData data = findData(command);
    String value = data.value();
    StringReader reader = getReader(value);

    ParseExceptionFactory exceptions = new ParseExceptionFactory(reader);

    Lexer lexer = new Lexer(reader, exceptions);
    Parser parser = new Parser(lexer, defaultExecutes, defaultRule);
    RootTree tree = parser.parse();

    Map<String, Object> variables = new HashMap<>();
    variables.putAll(this.variables);
    variables.putAll(initializeLocalVariables(command));

    CompileErrors errors = new CompileErrors();

    CompileContext context = new CompileContext(
        variables,
        loader,
        typeRegistry,
        command,
        defaultPermissionFormat,
        errors,
        new Stack<>(),
        new HashMap<>(),
        new ArrayList<>()
    );

    GrenadierCommandNode node = (GrenadierCommandNode)
        tree.accept(CommandCompiler.COMPILER, context);

    String name = node.getName();

    if (name.equals("FAILED")) {
      name = command.getClass().getSimpleName();
    }

    if (!errors.getErrors().isEmpty()) {
      int error = errors.errorCount();

      // The error list contains not only fatal compile errors but also
      // warnings, so only throw if the fatal error count is above 0
      if (error > 0) {
        throw new CommandCompilationException(errors.getErrors(), reader, name);
      }

      if (areWarningsEnabled()) {
        String msg = CommandCompilationException.createMessage(
            errors.getErrors(),
            reader,
            name
        );

        System.out.print(msg);
        System.out.print("\n");
      }
    }

    CommandDispatcher<CommandSource> dispatcher = Grenadier.dispatcher();
    dispatcher.getRoot().addChild(node);

    if (syntaxConsumer != null) {
      context.consumeSyntax(node, syntaxConsumer);
    }

    return node;
  }

  CommandData findData(Object o) {
    Class<?> type = o.getClass();
    CommandData data = type.getAnnotation(CommandData.class);

    Preconditions.checkState(
        data != null,
        "Class '%s' had no CommandData annotation",
        type.getName()
    );

    return data;
  }

  @SuppressWarnings("deprecation")
  Map<String, Object> initializeLocalVariables(Object o) {
    Class<?> type = o.getClass();

    Map<String, Object> variables = new VariableMap();

    for (var m: type.getDeclaredMethods()) {
      if (!m.isAnnotationPresent(VariableInitializer.class)) {
        continue;
      }

      Preconditions.checkState(
          m.getReturnType() == Void.TYPE,
          "Local command variable initializer '%s' must return void",
          m.toString()
      );

      Preconditions.checkState(
          m.getParameterCount() == 1 && m.getParameterTypes()[0] == Map.class,

          "Local command variable initializer '%s' must have "
              + "1 Map<String, Object> parameter",

          m.getName()
      );

      try {
        boolean override = m.isAccessible();
        m.setAccessible(true);

        m.invoke(o, variables);

        m.setAccessible(override);
      } catch (ReflectiveOperationException exc) {
        Utils.sneakyThrow(exc);
      }
    }

    return variables;
  }

  static class VariableMap extends HashMap<String, Object> {

    void validateKey(String s) {
      Objects.requireNonNull(s, "Null key");

      Preconditions.checkArgument(!s.isBlank(), "'%s' is blank");

      char first = s.charAt(0);

      Preconditions.checkArgument(
          Character.isJavaIdentifierStart(first),
          "Illegal starting character '%s'", first
      );

      if (s.length() > 1) {
        char[] chars = s.toCharArray();

        for (int i = 1; i < s.length(); i++) {
          Preconditions.checkArgument(
              Character.isJavaIdentifierPart(chars[i]),
              "Illegal character '%s' at index %s", chars[i], i
          );
        }
      }
    }

    @Override
    public Object put(String key, Object value) {
      validateKey(key);
      return super.put(key, value);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
      validateKey(key);
      return super.putIfAbsent(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
      m.forEach((s, o) -> validateKey(s));
      super.putAll(m);
    }

    @Override
    public Object compute(String key,
                          BiFunction<? super String, ? super Object, ?> remappingFunction
    ) {
      validateKey(key);
      return super.compute(key, remappingFunction);
    }

    @Override
    public Object computeIfAbsent(String key,
                                  Function<? super String, ?> mappingFunction
    ) {
      validateKey(key);
      return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key,
                                   BiFunction<? super String, ? super Object, ?> remappingFunction
    ) {
      validateKey(key);
      return super.computeIfPresent(key, remappingFunction);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
      validateKey(key);
      return super.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(String key, Object value) {
      validateKey(key);
      return super.replace(key, value);
    }
  }
}
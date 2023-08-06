package net.forthecrown.grenadier.annotations;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import net.forthecrown.grenadier.annotations.util.ErrorMessages;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.annotations.util.Utils;
import org.jetbrains.annotations.NotNull;

final class AnnotatedCommandContextImpl implements AnnotatedCommandContext {

  private static final Pattern PRE_PROCESS_PATTERN
      = Pattern.compile("#([a-zA-Z_$][a-zA-Z0-9_$]*)(?:\\((.*)\\))?");

  static final int GROUP_DIRECTIVE = 1;
  static final int GROUP_ARGUMENTS = 2;

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

  private Result<StringReader> getReader(String value) {
    StringReader reader = new StringReader(value);

    if (value.isEmpty()) {
      return Result.fail("Empty input");
    }

    if (!Readers.startsWith(reader, "file")) {
      return Result.success(reader);
    }

    reader.setCursor(reader.getCursor() + "file".length());
    reader.skipWhitespace();

    try {
      reader.expect('=');
    } catch (CommandSyntaxException e) {
      return Result.fail(reader.getCursor(), "'file' token must be proceeded by '=' token")
          .mapError((pos, message) -> ErrorMessages.formatError(reader, pos, message))
          .cast();
    }

    reader.skipWhitespace();

    int c = reader.getCursor();
    String path = reader.getRemaining();
    reader.setCursor(reader.getTotalLength());

    return findLoaderFile(path, c)
        .mapError((pos, message) -> ErrorMessages.formatError(reader, pos, message))
        .map(StringReader::new);
  }

  Result<String> findLoaderFile(String path, int pos) {
    for (var l: loaders) {
      String inputString;

      try {
        inputString = l.getString(path);
      } catch (IOException exc) {
        exc.printStackTrace(System.err);
        continue;
      }

      if (inputString == null || inputString.isEmpty()) {
        continue;
      }

      return Result.success(inputString);
    }

    return Result.fail(pos, "No valid loader path '%s' found", path);
  }

  Result<StringReader> getInput(Object commandObject) {
    Class<?> type = commandObject.getClass();
    CommandData data = type.getAnnotation(CommandData.class);

    Result<StringReader> input;

    if (data != null) {
      input = getReader(data.value());
    } else {
      CommandFile file = type.getAnnotation(CommandFile.class);

      if (file == null) {
        return Result.fail("No CommandData or CommandFile annotation present");
      }

      String path = file.value();

      if (path.isEmpty()) {
        return Result.fail("No file path set");
      }

      input = findLoaderFile(path, 0)
          .mapError((pos, message) -> {
            return ErrorMessages.formatError(new StringReader(path), pos, message);
          })
          .map(StringReader::new);
    }

    return input.flatMap(this::resolvePreProcessTokens);
  }

  @Override
  public GrenadierCommandNode registerCommand(
      Object command,
      ClassLoader loader
  ) throws CommandParseException, CommandCompilationException {
    StringReader reader = getInput(command).orThrow((position, message) -> {
      return new CommandParseException(
          "Error getting command input for " + command + ": " + message
      );
    });

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

  Result<StringReader> resolvePreProcessTokens(StringReader reader) {
    ParseExceptionFactory factory = new ParseExceptionFactory(reader);

    String str = reader.getString();
    StringBuilder result = new StringBuilder();

    Matcher matcher = PRE_PROCESS_PATTERN.matcher(str);

    while (matcher.find()) {
      String directive = matcher.group(GROUP_DIRECTIVE);
      String arguments = matcher.group(GROUP_ARGUMENTS);

      Result<String> replacement = executePreProcess(
          directive,
          arguments,
          matcher.start()
      );

      if (replacement.isError()) {
        return replacement.mapError(factory::format).cast();
      }

      matcher.appendReplacement(result, replacement.getValue());
    }

    matcher.appendTail(result);

    return Result.success(new StringReader(result.toString()));
  }

  Result<String> executePreProcess(
      String dir,
      String args,
      int start
  ) {
    return switch (dir) {
      case "paste" -> {
        if (Strings.isNullOrEmpty(args)) {
          yield Result.fail(start, "'paste' preprocessor requires filename to paste from");
        }

        yield findLoaderFile(args, start);
      }

      default -> Result.fail(start, "Invalid preprocessor directive '%s'", dir);
    };
  }

  static class VariableMap extends HashMap<String, Object> {

    void validateKey(String s) {
      Objects.requireNonNull(s, "Null key");
      Preconditions.checkArgument(!s.isBlank(), "'%s' is blank");

      char first = s.charAt(0);

      Preconditions.checkArgument(
          Character.isJavaIdentifierStart(first),
          "Illegal starting character '%s', must be a-z, A-Z, '_' or '$'",
          first
      );

      if (s.length() > 1) {
        char[] chars = s.toCharArray();

        for (int i = 1; i < s.length(); i++) {
          Preconditions.checkArgument(
              Character.isJavaIdentifierPart(chars[i]),

              "Illegal character '%s' at index %s, must be a-z, A-Z, 0-9, '_' "
                  + "or '$",
              chars[i], i
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
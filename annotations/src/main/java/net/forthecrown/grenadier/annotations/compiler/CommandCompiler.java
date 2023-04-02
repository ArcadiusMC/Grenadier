package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.Nodes;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.TypeRegistry.TypeParser;
import net.forthecrown.grenadier.annotations.tree.AbstractCmdTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.InvokeResultMethod;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.RefMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.VariableMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.TypeInfoTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.VariableTypeRef;
import net.forthecrown.grenadier.annotations.tree.ChildCommandTree;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.RefExecution;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.VariableExecutes;
import net.forthecrown.grenadier.annotations.tree.LiteralTree;
import net.forthecrown.grenadier.annotations.tree.Name;
import net.forthecrown.grenadier.annotations.tree.Name.DirectName;
import net.forthecrown.grenadier.annotations.tree.Name.FieldRefName;
import net.forthecrown.grenadier.annotations.tree.Name.VariableName;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.ConstantRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.PermissionRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.RequiresRef;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.VariableRequires;
import net.forthecrown.grenadier.annotations.tree.RootTree;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.ComponentRefSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.StringListSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.VariableSuggestions;
import net.forthecrown.grenadier.annotations.tree.TreeVisitor;
import net.forthecrown.grenadier.annotations.util.Result;

public class CommandCompiler implements TreeVisitor<Object, CompileContext> {

  private final String FAILED = "FAILED";

  public static final CommandCompiler COMPILER = new CommandCompiler();

  private void consumeName(Name name,
                           CompileContext context,
                           Consumer<String> consumer
  ) {
    Result<String> res = (Result<String>) name.accept(this, context);
    res.apply(context.getErrors(), consumer);
  }

  @Override
  public Result<SuggestionProvider> visitStringSuggestions(
      StringListSuggestions tree,
      CompileContext context
  ) {
    String[] suggestions = tree.suggestions();

    return Result.success((context1, builder) -> {
      return Completions.suggest(builder, suggestions);
    });
  }

  @Override
  public Result<SuggestionProvider> visitRefSuggestions(
      ComponentRefSuggestions tree,
      CompileContext context
  ) {
    return CompiledSuggester.compile(tree, context);
  }

  @Override
  public Result<SuggestionProvider> visitVariableSuggests(
      VariableSuggestions tree,
      CompileContext context
  ) {
    return context.getVariable(tree, SuggestionProvider.class);
  }

  @Override
  public Result<Predicate<CommandSource>> visitPermissionRequirement(
      PermissionRequires tree,
      CompileContext context
  ) {
    Result<String> permissionResult
        = (Result<String>) tree.name().accept(this, context);

    return permissionResult.map(permission -> {
      return source -> source.hasPermission(permission);
    });
  }

  @Override
  public Result<Predicate> visitRefRequires(
      RequiresRef tree,
      CompileContext context
  ) {
    return CompiledRequires.compile(tree, context);
  }

  @Override
  public Result<Predicate> visitConstantRequires(ConstantRequires tree,
                                                 CompileContext context
  ) {
    boolean value = tree.value();
    return Result.success(o -> value);
  }

  @Override
  public Result<Predicate> visitVariableRequires(
      VariableRequires tree,
      CompileContext context
  ) {
    return context.getVariable(tree, Predicate.class);
  }

  @Override
  public CommandNode<CommandSource> visitLiteral(LiteralTree tree,
                                                 CompileContext context
  ) {
    Result<String> nameResult = (Result<String>)
        tree.getName().accept(this, context);

    nameResult.report(context.getErrors());

    String name = nameResult.orElse(FAILED);
    var literal = Nodes.literal(name);

    genericChildVisit(tree, literal, context, name);

    return literal.build();
  }

  @Override
  public CommandNode<CommandSource> visitArgument(ArgumentTree tree,
                                                  CompileContext context
  ) {
    Result<ArgumentType> typeResult = (Result<ArgumentType>)
        tree.getTypeInfo().accept(this, context);

    typeResult.report(context.getErrors());

    ArgumentType<?> type = typeResult.isError()
        ? StringArgumentType.word()
        : typeResult.getValue();

    Result<String> nameResult = (Result<String>)
        tree.getName().accept(this, context);

    nameResult.report(context.getErrors());

    String name = nameResult.orElse(FAILED);

    if (!nameResult.isError()) {
      context.pushArgument(name);
    }

    var builder = Nodes.argument(name, type);

    if (tree.getSuggests() != null) {
      Result<SuggestionProvider<CommandSource>> providerResult
          = (Result<SuggestionProvider<CommandSource>>)
          tree.getSuggests().accept(this, context);

      providerResult.apply(context.getErrors(), builder::suggests);
    }

    genericChildVisit(tree, builder, context, name);

    if (!nameResult.isError()) {
      context.popArgument();
    }

    return builder.build();
  }

  @Override
  public GrenadierCommandNode visitRoot(RootTree tree, CompileContext context) {
    Result<String> nameResult = (Result<String>)
        tree.getName().accept(this, context);

    nameResult.report(context.getErrors());

    String name = nameResult.orElse(FAILED);
    GrenadierCommand builder = new GrenadierCommand(name);

    if (tree.getDescription() != null) {
      builder.withDescription(tree.getDescription());
    }

    if (tree.getPermission() != null) {
      consumeName(tree.getPermission(), context, s -> {
        String permission = s.replace("{command}", name);
        builder.withPermission(permission);
      });
    } else {
      builder.withPermission(context.defaultedPermission(name));
    }

    if (tree.getAliases() != null && !tree.getAliases().isEmpty()) {
      tree.getAliases().forEach(name1 -> {
        consumeName(name1, context, s -> builder.getAliases().add(s));
      });
    }

    genericNodeVisit(tree, builder, context);
    return builder.build();
  }

  private void genericChildVisit(ChildCommandTree tree,
                                 ArgumentBuilder<CommandSource, ?> builder,
                                 CompileContext context,
                                 String name
  ) {
    context = compileMappers(tree, context, name);
    genericNodeVisit(tree, builder, context);
  }

  private void genericNodeVisit(AbstractCmdTree tree,
                                ArgumentBuilder<CommandSource, ?> builder,
                                CompileContext context
  ) {
    if (tree.getRequires() != null) {
      Result<Predicate<CommandSource>> predicateResult
          = (Result<Predicate<CommandSource>>)
          tree.getRequires().accept(this, context);

      predicateResult.apply(context.getErrors(), builder::requires);
    }

    if (tree.getExecutes() != null) {
      Result<Command<CommandSource>> cmdResult
          = (Result<Command<CommandSource>>)
          tree.getExecutes().accept(this, context);

      cmdResult.apply(context.getErrors(), builder::executes);
    }

    tree.getChildren().forEach(child -> {
      CommandNode<CommandSource> node
          = (CommandNode<CommandSource>) child.accept(this, context);

      builder.then(node);
    });
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CompileContext compileMappers(ChildCommandTree tree,
                                        CompileContext context,
                                        String argumentName
  ) {
    if (tree.getMappers() == null || tree.getMappers().isEmpty()) {
      return context;
    }

    CompileContext result = context;

    for (var m: tree.getMappers()) {
      Result<ArgumentModifier> modifierResult
          = (Result<ArgumentModifier>) m.accept(this, context);

      String modifierArgumentName;
      if (m.argumentName() == null) {
        modifierArgumentName = argumentName;
      } else {
        Result<String> mapperNameResult = (Result<String>)
            m.argumentName().accept(this, context);

        mapperNameResult.report(context.getErrors());
        modifierArgumentName = mapperNameResult.orElse(FAILED);

        if (!mapperNameResult.isError()) {
          boolean argumentExists = context.getAvailableArguments()
              .contains(modifierArgumentName);

          if (!argumentExists) {
            context.getErrors().warning(
                m.argumentName().tokenStart(),
                "Argument '%s' doesn't exist in current scope",
                modifierArgumentName
            );
          }
        }
      }

      modifierResult.report(context.getErrors());

      if (!modifierResult.isError()) {
        result = context.withModifier(
            modifierArgumentName,
            modifierResult.getValue()
        );
      }
    }

    return result;
  }

  @Override
  public Result<ArgumentType> visitTypeInfo(TypeInfoTree tree,
                                            CompileContext context
  ) {
    var registry = context.getTypeRegistry();
    TypeParser<?> parser = registry.getParser(tree.name());

    if (parser == null) {
      return Result.fail(tree.tokenStart(),
          "Unknown argument type '%s'", tree.name()
      );
    }

    return (Result<ArgumentType>) parser.parse(tree, context);
  }

  @Override
  public Result<ArgumentType> visitVariableType(VariableTypeRef tree,
                                                CompileContext context
  ) {
    return context.getVariable(tree, ArgumentType.class);
  }

  @Override
  public Result<Command> visitVarExec(VariableExecutes tree,
                                      CompileContext context
  ) {
    return context.getVariable(tree, Command.class);
  }

  @Override
  public Result<Command> visitRefExec(RefExecution tree,
                                      CompileContext context
  ) {
    return CompiledExecutes.compile(tree, context);
  }

  @Override
  public Result<String> visitVariableName(VariableName tree,
                                          CompileContext context
  ) {
    return context.getVariable(tree, String.class);
  }

  @Override
  public Result<String> visitDirectName(DirectName tree,
                                        CompileContext context
  ) {
    return Result.success(tree.value());
  }

  @Override
  public Result<String> visitFieldName(FieldRefName tree,
                                       CompileContext context
  ) {
    Class<?> cmdClass = context.getCommandClass().getClass();
    Field nameField;

    try {
      nameField = cmdClass.getDeclaredField(tree.fieldName());
    } catch (ReflectiveOperationException exc) {
      return Result.fail(
          "Reflection error while accessing field '%s': %s",
          tree.fieldName(), exc.getMessage()
      );
    }

    if (!Modifier.isFinal(nameField.getModifiers())) {
      context.getErrors().warning(
          "Field '%s' in '%s' is not final! Changes to "
              + "this field will not be reflected in the command tree",

          nameField.getName(), nameField.getDeclaringClass()
      );
    }

    if (CharSequence.class.isAssignableFrom(nameField.getDeclaringClass())) {
      return Result.fail(
          "Cannot get name from field '%s' in %s. "
              + "Field's type is not an inheritor of java.lang.CharSequence",

          nameField.getName(), nameField.getDeclaringClass()
      );
    }

    try {
      Object o = nameField.get(context.getCommandClass());
      CharSequence sequence = (CharSequence) o;

      if (sequence == null) {
        return Result.fail(
            "Field '%s' in %s returned null! Cannot get name",
            nameField.getName(), nameField.getDeclaringClass()
        );
      }

      return Result.success(sequence.toString());
    } catch (IllegalAccessException e) {
      return Result.fail(
          "Illegal access to field '%s' in %s. "
              + "This error shouldn't happen",

          nameField.getName(), nameField.getDeclaringClass()
      );
    }
  }

  @Override
  public Result<ArgumentModifier> visitVarModifier(VariableMapper tree,
                                                   CompileContext context
  ) {
    return context.getVariable(tree, ArgumentModifier.class);
  }

  @Override
  public Result<ArgumentModifier> visitRefModifier(RefMapper tree,
                                                   CompileContext context
  ) {
    // TODO perform validation of reference before returning success
    return Result.success(
        new CompiledArgumentMapper(context.getCommandClass(), tree.ref())
    );
  }

  @Override
  public Object visitResultInvokeModifier(InvokeResultMethod tree,
                                          CompileContext context
  ) {
    // TODO perform validation of reference before returning success
    return Result.success(
        new CompiledArgumentMapper(null, tree.ref())
    );
  }
}
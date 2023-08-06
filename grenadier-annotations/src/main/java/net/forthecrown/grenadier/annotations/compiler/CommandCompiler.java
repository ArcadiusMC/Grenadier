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
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.MemberMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.ResultMemberMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentMapperTree.VariableMapper;
import net.forthecrown.grenadier.annotations.tree.ArgumentTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeTree.TypeInfoTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeTree.VariableTypeReference;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.ArrayDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.LiteralDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.TranslatableDescription;
import net.forthecrown.grenadier.annotations.tree.DescriptionTree.VariableDescription;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.MemberExecutes;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.VariableExecutes;
import net.forthecrown.grenadier.annotations.tree.LiteralTree;
import net.forthecrown.grenadier.annotations.tree.Name;
import net.forthecrown.grenadier.annotations.tree.Name.DirectName;
import net.forthecrown.grenadier.annotations.tree.Name.FieldReferenceName;
import net.forthecrown.grenadier.annotations.tree.Name.VariableName;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.ConstantRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.MemberRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.PermissionRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.VariableRequires;
import net.forthecrown.grenadier.annotations.tree.RootTree;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.MemberSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.StringListSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.VariableSuggestions;
import net.forthecrown.grenadier.annotations.tree.TreeVisitor;
import net.forthecrown.grenadier.annotations.util.Result;
import net.kyori.adventure.text.Component;
import org.bukkit.permissions.Permission;

public class CommandCompiler implements TreeVisitor<Object, CompileContext> {

  private static final String FAILED = "FAILED";

  public static final CommandCompiler COMPILER = new CommandCompiler();

  /* ------------------------------- NODES -------------------------------- */

  @Override
  public CommandNode<CommandSource> visitLiteral(LiteralTree tree, CompileContext context) {
    Result<String> nameResult = (Result<String>)
        tree.getName().accept(this, context);

    nameResult.report(context.getErrors());

    String name = nameResult.orElse(FAILED);
    var literal = Nodes.literal(name);

    genericNodeVisit(tree, literal, context, name);

    return literal.build();
  }

  @Override
  public CommandNode<CommandSource> visitArgument(ArgumentTree tree, CompileContext context) {
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
      if (context.getAvailableArguments().contains(name)) {
        context.getErrors().error(tree.getName().tokenStart(),
            "Duplicate argument name, %s already declared", name
        );
      }

      context.pushArgument(name);
    }

    var builder = Nodes.argument(name, type);

    if (tree.getSuggests() != null) {
      Result<SuggestionProvider<CommandSource>> providerResult
          = (Result<SuggestionProvider<CommandSource>>)
          tree.getSuggests().accept(this, context);

      providerResult.apply(context.getErrors(), builder::suggests);
    }

    genericNodeVisit(tree, builder, context, name);

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

    builder.withPlainTranslation(tree.isPlainTranslation());

    if (tree.getPermission() != null) {
      consumeName(tree.getPermission(), context, s -> {
        String permission = s.replace("{command}", name);
        builder.withPermission(permission);
        context.pushCondition(new PermissionPredicate(permission));
      });
    } else {
      builder.withPermission(context.defaultedPermission(name));
    }

    if (tree.getAliases() != null && !tree.getAliases().isEmpty()) {
      tree.getAliases().forEach(name1 -> {
        consumeName(name1, context, s -> builder.getAliases().add(s));
      });
    }

    genericNodeVisit(tree, builder, context, name);
    return builder.build();
  }

  private void genericNodeVisit(AbstractCmdTree tree,
                                ArgumentBuilder<CommandSource, ?> builder,
                                CompileContext t_context,
                                String name
  ) {
    var context = compileMappers(tree, t_context, name);

    String argumentLabel;

    if (tree.getSyntaxLabel() == null) {
      argumentLabel = tree instanceof ArgumentTree
          ? "<%s>".formatted(name)
          : name;

    } else {
      Result<String> argLabel
          = (Result<String>) tree.getSyntaxLabel().accept(this, context);

      argLabel.report(context.getErrors());
      argumentLabel = argLabel.orElse(FAILED);
    }

    context.pushPrefix(argumentLabel);

    if (tree.getDescription() != null) {
      final var fContext = context;

      consumeDescription(tree.getDescription(), context, component -> {
        if (builder instanceof GrenadierCommand gren) {
          if (tree.getExecutes() != null) {
            consumeSyntax(fContext, component);
          }

          gren.withDescription(component);
          return;
        }

        consumeSyntax(fContext, component);
      });
    }

    final boolean[] conditionPushed = new boolean[1];
    if (tree.getRequires() != null) {
      Result<Predicate<CommandSource>> predicateResult
          = (Result<Predicate<CommandSource>>)
          tree.getRequires().accept(this, context);

      predicateResult.apply(context.getErrors(), predicate -> {
        conditionPushed[0] = true;
        context.pushCondition(predicate);
        builder.requires(predicate);
      });
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

    context.popPrefix();

    if (conditionPushed[0]) {
      context.popCondition();
    }
  }

  private void consumeDescription(DescriptionTree tree,
                                  CompileContext context,
                                  Consumer<Component> consumer
  ) {
    Result<Component> descRes = (Result<Component>) tree.accept(this, context);
    descRes.apply(context.getErrors(), consumer);
  }

  private void consumeSyntax(CompileContext context,
                             Component component
  ) {
    String label = context.syntaxPrefix();
    context.getSyntaxList().add(label, component, context.buildConditions());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private CompileContext compileMappers(AbstractCmdTree tree,
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

  private void consumeName(Name name,
                           CompileContext context,
                           Consumer<String> consumer
  ) {
    Result<String> res = (Result<String>) name.accept(this, context);
    res.apply(context.getErrors(), consumer);
  }

  /* ---------------------------- SUGGESTIONS ----------------------------- */

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
  public Result<SuggestionProvider> visitMemberSuggestions(
      MemberSuggestions tree,
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

  /* ---------------------------- REQUIREMENTS ---------------------------- */

  @Override
  public Result<Predicate<CommandSource>> visitPermissionRequirement(
      PermissionRequires tree,
      CompileContext context
  ) {
    Result<String> permissionResult
        = (Result<String>) tree.name().accept(this, context);

    return permissionResult.map(PermissionPredicate::new);
  }

  @Override
  public Result<Predicate> visitMemberRequirement(
      MemberRequires tree,
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

  /* ------------------------ ARGUMENT TYPE INFOS ------------------------- */

  @Override
  public Result<ArgumentType> visitArgumentTypeTree(TypeInfoTree tree,
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
  public Result<ArgumentType> visitVariableArgumentType(VariableTypeReference tree,
                                                        CompileContext context
  ) {
    return context.getVariable(tree, ArgumentType.class);
  }

  /* ----------------------------- EXECUTIONS ----------------------------- */

  @Override
  public Result<Command> visitVariableExecutes(VariableExecutes tree,
                                               CompileContext context
  ) {
    return context.getVariable(tree, Command.class);
  }

  @Override
  public Result<Command> visitMemberExecutes(MemberExecutes tree,
                                             CompileContext context
  ) {
    return CompiledExecutes.compile(tree, context);
  }

  /* ------------------------------- NAMES -------------------------------- */

  @Override
  public Result<String> visitVariableName(VariableName tree, CompileContext context) {
    return context.getVariable(tree, String.class)
        .or(() -> context.getVariable(tree, Permission.class).map(Permission::getName));
  }

  @Override
  public Result<String> visitDirectName(DirectName tree, CompileContext context) {
    return Result.success(tree.value());
  }

  @Override
  public Result<String> visitFieldName(FieldReferenceName tree, CompileContext context) {
    int start = tree.tokenStart();
    Class<?> cmdClass = context.getCommandClass().getClass();
    Field nameField;

    try {
      nameField = cmdClass.getDeclaredField(tree.fieldName());
    } catch (ReflectiveOperationException exc) {
      return Result.fail(start,
          "Reflection error while accessing field '%s': %s",
          tree.fieldName(), exc.getMessage()
      );
    }

    if (!Modifier.isFinal(nameField.getModifiers())) {
      context.getErrors().warning(start,
          "Field '%s' in '%s' is not final! Changes to "
              + "this field will not be reflected in the command tree",

          nameField.getName(), nameField.getDeclaringClass()
      );
    }

    if (CharSequence.class.isAssignableFrom(nameField.getDeclaringClass())) {
      return Result.fail(start,
          "Cannot get name from field '%s' in %s. "
              + "Field's type is not an inheritor of java.lang.CharSequence",

          nameField.getName(), nameField.getDeclaringClass()
      );
    }

    try {
      var overriden = nameField.isAccessible();
      nameField.setAccessible(true);

      Object o = nameField.get(context.getCommandClass());

      nameField.setAccessible(overriden);

      CharSequence sequence = (CharSequence) o;

      if (sequence == null) {
        return Result.fail(start,
            "Field '%s' in %s returned null! Cannot get name",
            nameField.getName(), nameField.getDeclaringClass()
        );
      }

      return Result.success(sequence.toString());
    } catch (IllegalAccessException e) {
      return Result.fail(start,
          "Illegal access to field '%s' in %s. "
              + "This error shouldn't happen",

          nameField.getName(), nameField.getDeclaringClass()
      );
    }
  }

  /* --------------------------- RESULT MAPPERS --------------------------- */

  @Override
  public Result<ArgumentModifier> visitVariableMapper(VariableMapper tree, CompileContext context) {
    return context.getVariable(tree, ArgumentModifier.class);
  }

  @Override
  public Result<ArgumentModifier> visitMemberMapper(MemberMapper tree, CompileContext context) {
    // TODO perform validation of reference before returning success
    return Result.success(
        new CompiledArgumentMapper(context.getCommandClass(), tree.ref())
    );
  }

  @Override
  public Object visitResultMemberMapper(ResultMemberMapper tree, CompileContext context) {
    // TODO perform validation of reference before returning success
    return Result.success(
        new CompiledArgumentMapper(null, tree.ref())
    );
  }

  /* ---------------------------- DESCRIPTION ----------------------------- */

  @Override
  public Result<Component> visitLiteralDescription(
      LiteralDescription tree,
      CompileContext context
  ) {
    return Result.success(Component.text(tree.value()));
  }

  @Override
  public Result<Component> visitVariableDescription(
      VariableDescription tree,
      CompileContext context
  ) {
    return context.getVariable(tree, Component.class);
  }

  @Override
  public Result<Component> visitTranslatableDescription(
      TranslatableDescription tree,
      CompileContext context
  ) {
    return Result.success(
        Component.translatable(tree.translationKey())
    );
  }

  @Override
  public Result<Component> visitArrayDescription(
      ArrayDescription tree,
      CompileContext context
  ) {
    if (tree.elements().length < 1) {
      return Result.fail(tree.tokenStart(), "Empty description");
    }

    var builder = Component.text();
    final boolean[] addedAny = { false };

    for (DescriptionTree element : tree.elements()) {
      Result<Component> res
          = (Result<Component>) element.accept(this, context);

      res.apply(context.getErrors(), component -> {
        if (addedAny[0]) {
          builder.append(Component.newline());
        }

        builder.append(component);
        addedAny[0] = true;
      });
    }

    if (addedAny[0]) {
      return Result.success(builder.build());
    } else {
      return Result.fail(tree.tokenStart(),
          "Failed to read any elements for description"
      );
    }
  }
}
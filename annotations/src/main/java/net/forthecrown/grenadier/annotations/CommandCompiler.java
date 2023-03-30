package net.forthecrown.grenadier.annotations;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.Pair;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.Nodes;
import net.forthecrown.grenadier.annotations.tree.AbstractCmdTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTree;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef;
import net.forthecrown.grenadier.annotations.tree.LiteralTree;
import net.forthecrown.grenadier.annotations.tree.Name.DirectName;
import net.forthecrown.grenadier.annotations.tree.Name.VariableName;
import net.forthecrown.grenadier.annotations.TypeRegistry.TypeParser;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.TypeInfoTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.VariableTypeRef;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef.Kind;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.RefExecution;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.VariableExecutes;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.PermissionRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.RequiresRef;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.VariableRequires;
import net.forthecrown.grenadier.annotations.tree.RootTree;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.ComponentRefSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.StringListSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.VariableSuggestions;
import net.forthecrown.grenadier.annotations.tree.TreeVisitor;

class CommandCompiler implements TreeVisitor<Object, CompilationContext> {

  public static final CommandCompiler COMPILER
      = new CommandCompiler();

  @Override
  public SuggestionProvider<CommandSource> visitStringSuggestions(
      StringListSuggestions tree,
      CompilationContext context
  ) {
    String[] arr = tree.suggestions();
    return (context1, builder) -> Completions.suggest(builder, arr);
  }

  @Override
  public SuggestionProvider<CommandSource> visitRefSuggestions(
      ComponentRefSuggestions tree,
      CompilationContext context
  ) {
    ClassComponentRef ref = tree.ref();
    Object commandClass = context.commandClass();

    return new CompiledSuggester(ref, commandClass);
  }

  @Override
  public SuggestionProvider<CommandSource> visitVariableSuggests(
      VariableSuggestions tree,
      CompilationContext context
  ) {
    return context.getOrThrow(tree.variable(), SuggestionProvider.class);
  }

  @Override
  public Predicate<CommandSource> visitPermissionRequirement(
      PermissionRequires tree,
      CompilationContext context
  ) {
    String permission = tree.name().accept(this, context).toString();
    return source -> source.hasPermission(permission);
  }

  @Override
  public Predicate<CommandSource> visitRefRequires(
      RequiresRef tree,
      CompilationContext context
  ) {
    ClassComponentRef ref = tree.ref();
    return new CompiledRequires(ref, context.commandClass());
  }

  @Override
  public Predicate<CommandSource> visitVariableRequires(
      VariableRequires tree,
      CompilationContext context
  ) {
    return context.getOrThrow(tree.variable(), Predicate.class);
  }

  @Override
  public CommandNode<CommandSource> visitLiteral(LiteralTree tree,
                                                 CompilationContext context
  ) {
    String name = tree.getName().accept(this, context).toString();
    var literal = Nodes.literal(name);
    genericVisit(literal, tree, context);
    return literal.build();
  }

  @Override
  public CommandNode<CommandSource> visitArgument(ArgumentTree tree,
                                                  CompilationContext context
  ) {
    ArgumentType<?> type
        = (ArgumentType<?>) tree.getTypeInfo().accept(this, context);

    String name = tree.getName().accept(this, context).toString();

    var argument = Nodes.argument(name, type);

    if (tree.getSuggests() != null) {
      SuggestionProvider<CommandSource> provider
          = (SuggestionProvider<CommandSource>)
          tree.getSuggests().accept(this, context);

      argument.suggests(provider);
    }

    genericVisit(argument, tree, context);
    return argument.build();
  }

  @Override
  public GrenadierCommandNode visitRoot(RootTree tree,
                                        CompilationContext context
  ) {
    String name = tree.getName().accept(this, context).toString();

    GrenadierCommand command = Grenadier.createCommand(name);

    if (tree.getAliases() != null) {
      command.withAliases(
          tree.getAliases()
              .stream()
              .map(name1 -> (String) name1.accept(this, context))
              .toList()
      );
    }

    if (tree.getPermission() != null) {
      command.withPermission(
          (String) tree.getPermission().accept(this, context)
      );
    } else if (context.defaultPermission() != null) {
      command.withPermission(context.defaultedPermission(name));
    }

    if (tree.getDescription() != null) {
      command.withDescription(tree.getDescription());
    }

    genericVisit(command, tree, context);

    return command.build();
  }

  private void genericVisit(ArgumentBuilder<CommandSource, ?> builder,
                            AbstractCmdTree tree,
                            CompilationContext context
  ) {
    if (tree.getRequires() != null) {
      Predicate<CommandSource> predicate = (Predicate<CommandSource>)
          tree.getRequires().accept(this, context);

      builder.requires(predicate);
    }

    if (tree.getExecutes() != null) {
      Command<CommandSource> command = (Command<CommandSource>)
          tree.getExecutes().accept(this, context);

      builder.executes(command);
    }

    for (var c: tree.getChildren()) {
      CommandNode<CommandSource> node
          = (CommandNode<CommandSource>) c.accept(this, context);

      builder.then(node);
    }
  }

  @Override
  public ArgumentType<?> visitTypeInfo(TypeInfoTree tree,
                                       CompilationContext context
  ) {
    TypeParser<?> parser = context.typeRegistry().getParser(tree.name());
    Objects.requireNonNull(parser, "Unknown argument type '" + tree.name() + "'");

    return parser.parse(tree, context, context.exceptions());
  }

  @Override
  public ArgumentType<?> visitVariableType(VariableTypeRef tree,
                                  CompilationContext context
  ) {
    return context.getOrThrow(tree.variable(), ArgumentType.class);
  }

  @Override
  public Command<CommandSource> visitVarExec(VariableExecutes tree,
                                             CompilationContext context
  ) {
    Command<CommandSource> command
        = context.getOrThrow(tree.variable(), Command.class);

    return command;
  }

  @Override
  public Command<CommandSource> visitRefExec(RefExecution tree,
                                             CompilationContext context
  ) {
    var ref = tree.ref();
    return new CompiledExecutes(ref, context.commandClass());
  }

  @Override
  public String visitDirectName(DirectName tree, CompilationContext context) {
    return tree.value();
  }

  @Override
  public String visitVariableName(VariableName tree, CompilationContext context) {
    return context.getOrThrow(tree.variable(), String.class);
  }

  @RequiredArgsConstructor
  private static class CompiledExecutes implements Command<CommandSource> {

    private final ClassComponentRef ref;
    private final Object commandClass;

    @Override
    public int run(CommandContext<CommandSource> context)
        throws CommandSyntaxException
    {
      Pair<Object, ClassComponentRef> last = ref.resolveLast(commandClass);

      ClassComponentRef lastRef = last.right();
      Object handle = last.left();

      if (lastRef.kind() == Kind.FIELD) {
        Object value = lastRef.resolve(handle);

        Preconditions.checkState(
            value instanceof Command<?>,
            "'%s' does not point to a Command<CommandSource> interface",
            ref.path()
        );

        return ((Command<CommandSource>) value)
            .run(context);
      }

      return lastRef.runAsExecutes(handle, context);
    }
  }

  @RequiredArgsConstructor
  private static class CompiledSuggester
      implements SuggestionProvider<CommandSource>
  {
    private static final Class<?>[] PARAMS = {
        CommandContext.class,
        SuggestionsBuilder.class
    };

    private final ClassComponentRef ref;
    private final Object commandClass;

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<CommandSource> context,
        SuggestionsBuilder builder
    ) throws CommandSyntaxException {

      return ref.execute(
          CompletableFuture.class,
          SuggestionProvider.class,
          suggestionProvider -> suggestionProvider.getSuggestions(context, builder),
          PARAMS,
          commandClass,

          context, builder
      );
    }
  }

  @RequiredArgsConstructor
  private static class CompiledRequires implements Predicate<CommandSource> {

    static final Class<?>[] PARAMS = { CommandSource.class };

    private final ClassComponentRef ref;
    private final Object commandClass;

    @Override
    public boolean test(CommandSource source) {
      try {
        return ref.execute(
            Boolean.TYPE,
            Predicate.class,
            predicate -> predicate.test(source),
            PARAMS,
            commandClass,
            source
        );
      } catch (CommandSyntaxException exc) {
        Utils.sneakyThrow(exc);
        return false;
      }
    }
  }
}
package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.FieldMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.MethodMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChainCompiler.ChainCompileConfig;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.MemberSuggestions;
import net.forthecrown.grenadier.annotations.util.ExpandedCommandContext;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.annotations.util.Utils;

@RequiredArgsConstructor
class CompiledSuggester implements SuggestionProvider<CommandSource> {

  private static final Class<?>[] PARAMS = {
      CommandContext.class,
      SuggestionsBuilder.class
  };

  private final MemberChain chain;
  private final Object commandClass;

  private final ParamFiller[] fillers;

  private final ContextFactory factory;

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) throws CommandSyntaxException {
    Pair<Object, MemberChain> lastChain;

    try {
      lastChain = chain.resolveLast(commandClass);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return Suggestions.empty();
    }

    Object o = lastChain.left();
    MemberChain chain = lastChain.right();

    if (chain instanceof FieldMember) {
      SuggestionProvider<CommandSource> provider
          = (SuggestionProvider<CommandSource>) chain.resolveSafe(o);

      return provider.getSuggestions(context, builder);
    }

    MethodMember member = (MethodMember) chain;
    Object[] params = new Object[member.method().getParameterCount()];

    var ctx = factory.create(context);

    for (int i = 0; i < fillers.length; i++) {
      ParamFiller filler = fillers[i];

      Objects.requireNonNull(filler,
          "Parameter filler at index " + i + " is missing"
      );

      params[i] = filler.fill(ctx, builder);
    }

    return (CompletableFuture<Suggestions>) member.invokeSafe(o, params);
  }

  public static Result<SuggestionProvider> compile(
      MemberSuggestions tree,
      CompileContext context
  ) {
    ChainCompileConfig config = ChainCompileConfig.create(tree.tokenStart())
        .setFinalFieldTypes(SuggestionProvider.class)
        .setValidReturnType(CompletableFuture.class);

    Class<?> declaring = context.getCommandClass().getClass();

    return MemberChainCompiler.compile(declaring, tree.ref(), config)
        .flatMap(chain -> compileFromChain(chain, context, tree.tokenStart()));
  }

  private static Result<SuggestionProvider> compileFromChain(
      MemberChain chain,
      CompileContext context,
      final int pos
  ) {
    MemberChain last = chain.getLastNode();

    if (last instanceof FieldMember) {
      return Result.success(
          new CompiledSuggester(chain, context.getCommandClass(), null, null)
      );
    }

    MethodMember member = (MethodMember) last;
    Method m = member.method();

    Parameter[] params = m.getParameters();
    ParamFiller[] fillers = new ParamFiller[params.length];

    int contextIndex = -1;
    int sourceIndex  = -1;
    int builderIndex = -1;

    for (int i = 0; i < params.length; i++) {
      Parameter p = params[i];
      Class<?> type = p.getType();

      if (type == CommandContext.class) {
        if (contextIndex != -1) {
          return Result.fail(pos,
              "CommandContext parameter declared more than once"
          );
        }

        contextIndex = i;
        fillers[i] = ParamFiller.FILL_CONTEXT;
        continue;
      }

      if (type == CommandSource.class) {
        if (sourceIndex != -1) {
          return Result.fail(pos,
              "CommandSource parameter declared more than once"
          );
        }

        sourceIndex = i;
        fillers[i] = ParamFiller.FILL_SOURCE;
        continue;
      }

      if (type == SuggestionsBuilder.class) {
        if (builderIndex != -1) {
          return Result.fail(pos,
              "SuggestionsBuilder parameter declared more than once"
          );
        }

        builderIndex = i;
        fillers[i] = ParamFiller.FILL_BUILDER;
        continue;
      }

      String argumentName;
      boolean optional;

      if (p.isAnnotationPresent(Argument.class)) {
        Argument argument = p.getAnnotation(Argument.class);
        argumentName = argument.value();
        optional = argument.optional();
      } else {
        argumentName = p.getName();
        optional = false;
      }

      if (!context.getAvailableArguments().contains(argumentName)
          && !optional
      ) {
        return Result.fail(pos,
            "Argument '%s' is not available in the current context",
            argumentName
        );
      }

      ArgumentParamFiller filler = new ArgumentParamFiller(
          Utils.primitiveToWrapper(p.getType()),
          argumentName,
          optional
      );

      fillers[i] = filler;
    }

    return Result.success(
        new CompiledSuggester(
            chain,
            context.getCommandClass(),
            fillers,
            context.createFactory()
        )
    );
  }

  @FunctionalInterface
  interface ParamFiller {
    ParamFiller FILL_CONTEXT = (context, builder) -> context.getBase();
    ParamFiller FILL_BUILDER = (context, builder) -> builder;
    ParamFiller FILL_SOURCE  = (context, builder) -> context.getSource();

    Object fill(ExpandedCommandContext context,
                SuggestionsBuilder builder
    );
  }

  @RequiredArgsConstructor
  static class ArgumentParamFiller implements ParamFiller {

    private final Class<?> type;
    private final String argumentName;
    private final boolean optional;

    @Override
    public Object fill(ExpandedCommandContext context,
                       SuggestionsBuilder builder
    ) {
      return context.getValue(argumentName, type, optional);
    }
  }
}
package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.FieldMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.MethodMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChainCompiler.ChainCompileConfig;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.MemberSuggestions;
import net.forthecrown.grenadier.annotations.util.Result;

class CompiledSuggester implements SuggestionProvider<CommandSource> {

  private final MemberChain chain;
  private final Object commandClass;

  private final ParamFiller[] fillers;
  private final Object[] parameterBuffer;

  private final ContextFactory factory;

  public CompiledSuggester(
      MemberChain chain,
      Object commandClass,
      ParamFiller[] fillers,
      ContextFactory factory
  ) {
    this.chain = chain;
    this.commandClass = commandClass;
    this.fillers = fillers;
    this.factory = factory;

    this.parameterBuffer = new Object[fillers.length];
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) throws CommandSyntaxException {
    Pair<Object, MemberChain> lastChain = chain.resolveLastSafe(commandClass);

    Object o = lastChain.left();
    MemberChain chain = lastChain.right();

    if (chain instanceof FieldMember) {
      SuggestionProvider<CommandSource> provider
          = (SuggestionProvider<CommandSource>) chain.resolveSafe(o);

      return provider.getSuggestions(context, builder);
    }

    MethodMember member = (MethodMember) chain;
    var ctx = factory.create(context);

    ParamFillers.fill(fillers, parameterBuffer, ctx, builder);
    Object result = member.invokeSafe(commandClass, parameterBuffer);

    return (CompletableFuture<Suggestions>) result;
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

    return ParamFillers.compile(m, context, true, pos).map(fillers -> {
      return new CompiledSuggester(
          chain,
          context.getCommandClass(),
          fillers,
          context.createFactory()
      );
    });
  }
}
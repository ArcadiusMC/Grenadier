package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.FieldMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.MethodMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChainCompiler.ChainCompileConfig;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.MemberExecutes;
import net.forthecrown.grenadier.annotations.util.ExpandedCommandContext;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.annotations.util.Utils;

@RequiredArgsConstructor
class CompiledExecutes implements Command<CommandSource> {

  private final Object commandClass;
  private final MemberChain chain;

  private final ContextFactory factory;
  private final ParamFiller[] fillers;

  @Override
  public int run(CommandContext<CommandSource> context)
      throws CommandSyntaxException
  {
    Pair<Object, MemberChain> last;

    try {
      last = chain.resolveLast(commandClass);
    } catch (Throwable t) {
      Utils.sneakyThrow(t);
      return 0;
    }

    Object o = last.left();
    MemberChain chain = last.right();

    if (chain instanceof FieldMember) {
      Command rawCommand = (Command) chain.resolveSafe(o);
      return rawCommand.run(context);
    }

    var ctx = factory.create(context);

    MethodMember member = (MethodMember) chain;
    Method method = member.method();

    Object[] params = new Object[method.getParameterCount()];

    for (int i = 0; i < params.length; i++) {
      ParamFiller filler = fillers[i];

      Objects.requireNonNull(filler,
          "Parameter filler at " + i + " is missing"
      );

      params[i] = filler.fill(ctx);
    }

    Object result = member.invokeSafe(commandClass, params);

    if (result instanceof Integer i) {
      return i;
    }

    return 0;
  }

  static Result<Command> compile(MemberExecutes tree, CompileContext context) {
    ChainCompileConfig config = ChainCompileConfig.create(tree.tokenStart())
        .setFinalFieldTypes(Command.class);

    Class<?> declaring = context.getCommandClass().getClass();

    return MemberChainCompiler.compile(declaring, tree.ref(), config)
        .flatMap(chain -> compileFromChain(chain, context, tree.tokenStart()));
  }

  private static Result<Command> compileFromChain(
      MemberChain chain,
      CompileContext context,
      int pos
  ) {
    MemberChain last = chain.getLastNode();

    if (last instanceof FieldMember) {
      return Result.success(
          new CompiledExecutes(context.getCommandClass(), last, null, null)
      );
    }

    MethodMember member = (MethodMember) last;
    Method method = member.method();

    Parameter[] parameters = method.getParameters();
    ParamFiller[] fillers = new ParamFiller[parameters.length];

    int contextIndex = -1;
    int sourceIndex  = -1;

    for (int i = 0; i < parameters.length; i++) {
      Parameter p = parameters[i];

      if (p.getType() == CommandContext.class) {
        if (contextIndex != -1) {
          return Result.fail(pos,
              "CommandContext<CommandSource> parameter set more than once"
          );
        }

        contextIndex = i;
        fillers[i] = ParamFiller.FILL_CONTEXT;
        continue;
      }

      if (p.getType() == CommandSource.class) {
        if (sourceIndex != -1) {
          return Result.fail(pos,
              "CommandSource parameter set more than once"
          );
        }

        sourceIndex = i;
        fillers[i] = ParamFiller.FILL_SOURCE;
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
          argumentName,
          Utils.primitiveToWrapper(p.getType()),
          optional
      );

      fillers[i] = filler;
    }

    return Result.success(
        new CompiledExecutes(
            context.getCommandClass(),
            chain,
            context.createFactory(),
            fillers
        )
    );
  }

  interface ParamFiller {
    ParamFiller FILL_SOURCE = ExpandedCommandContext::getSource;
    ParamFiller FILL_CONTEXT = ExpandedCommandContext::getBase;

    Object fill(ExpandedCommandContext context) throws CommandSyntaxException;
  }

  @RequiredArgsConstructor
  static class ArgumentParamFiller implements ParamFiller {

    private final String argumentName;
    private final Class<?> type;
    private final boolean optional;

    @Override
    public Object fill(ExpandedCommandContext context)
        throws CommandSyntaxException
    {
      return context.getValue(argumentName, type, optional);
    }
  }
}
package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.FieldMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.MethodMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChainCompiler.ChainCompileConfig;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.MemberExecutes;
import net.forthecrown.grenadier.annotations.util.Result;

class CompiledExecutes implements Command<CommandSource> {

  private final Object commandClass;
  private final MemberChain chain;

  private final ContextFactory factory;
  private final ParamFiller[] fillers;

  private final Object[] parameterBuffer;

  public CompiledExecutes(Object commandClass,
                          MemberChain chain,
                          ContextFactory factory,
                          ParamFiller[] fillers
  ) {
    this.commandClass = commandClass;
    this.chain = chain;
    this.factory = factory;
    this.fillers = fillers;
    this.parameterBuffer = new Object[fillers.length];
  }

  @Override
  public int run(CommandContext<CommandSource> context)
      throws CommandSyntaxException
  {
    Pair<Object, MemberChain> last = chain.resolveLastSafe(commandClass);

    Object o = last.left();
    MemberChain chain = last.right();

    if (chain instanceof FieldMember) {
      Command rawCommand = (Command) chain.resolveSafe(o);
      return rawCommand.run(context);
    }

    var ctx = factory.create(context);

    MethodMember member = (MethodMember) chain;

    ParamFillers.fill(fillers, parameterBuffer, ctx, null);
    Object result = member.invokeSafe(commandClass, parameterBuffer);

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

    return ParamFillers.compile(method, context, false, pos).map(fillers -> {
      return new CompiledExecutes(
          context.getCommandClass(),
          chain,
          context.createFactory(),
          fillers
      );
    });
  }
}
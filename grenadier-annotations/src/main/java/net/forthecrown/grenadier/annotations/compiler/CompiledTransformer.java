package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.CommandTransformer;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.FieldMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.MethodMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChainCompiler.ChainCompileConfig;
import net.forthecrown.grenadier.annotations.tree.TransformTree.MemberTransform;
import net.forthecrown.grenadier.annotations.util.Result;

class CompiledTransformer implements CommandTransformer {

  private final Object commandClass;
  private final MemberChain chain;
  private final Object[] params;

  CompiledTransformer(MemberChain chain, Object commandClass) {
    this.chain = chain;
    this.commandClass = commandClass;
    this.params = new Object[1];
  }

  @Override
  public <T extends ArgumentBuilder<CommandSource, T>> void apply(T node) {
    var last = chain.resolveLastSafe(commandClass);

    Object o = last.left();
    MemberChain lastMember = last.right();

    if (lastMember instanceof FieldMember) {
      ((CommandTransformer) o).apply(node);
      return;
    }

    params[0] = node;
    MethodMember member = (MethodMember) lastMember;

    member.invokeSafe(o, params);
  }

  static Result<CompiledTransformer> compile(CompileContext ctx, MemberTransform transform) {
    ChainCompileConfig config = ChainCompileConfig.create(transform.tokenStart())
        .setParamTypes(ArgumentBuilder.class)
        .setFinalFieldTypes(CommandTransformer.class)
        .setValidReturnType(Void.TYPE);

    return MemberChainCompiler.compile(ctx.getCommandClass().getClass(), transform.tree(), config)
        .flatMap(chain -> compileFromChain(ctx, chain));
  }

  static Result<CompiledTransformer> compileFromChain(CompileContext ctx, MemberChain chain) {
    return Result.success(new CompiledTransformer(chain, ctx.getCommandClass()));
  }
}

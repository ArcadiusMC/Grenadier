package net.forthecrown.grenadier.annotations.compiler;

import it.unimi.dsi.fastutil.Pair;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.FieldMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChain.MethodMember;
import net.forthecrown.grenadier.annotations.compiler.MemberChainCompiler.ChainCompileConfig;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.RequiresRef;
import net.forthecrown.grenadier.annotations.util.Result;
import net.forthecrown.grenadier.annotations.util.Utils;

@RequiredArgsConstructor
class CompiledRequires implements Predicate<CommandSource> {

  static final Class<?>[] PARAMS = {CommandSource.class};

  private final MemberChain chain;
  private final Object commandClass;

  @Override
  public boolean test(CommandSource source) {
    Pair<Object, MemberChain> last;

    try {
      last = chain.resolveLast(commandClass);
    } catch (Throwable t) {
      Utils.sneakyThrow(t);
      return false;
    }

    Object o = last.left();
    MemberChain chain = last.right();

    if (chain instanceof FieldMember) {
      Object res = chain.resolveSafe(o);

      if (res instanceof Predicate predicate) {
        return predicate.test(source);
      }

      return (Boolean) res;
    }

    MethodMember member = (MethodMember) chain;
    Object result = member.invokeSafe(o, source);

    return (Boolean) result;
  }

  static Result<Predicate> compile(
      RequiresRef tree,
      CompileContext context
  ) {
    Object o = context.getCommandClass();
    Class<?> type = o.getClass();
    int pos = tree.tokenStart();

    ChainCompileConfig config = ChainCompileConfig.create(pos)
        .setFinalFieldTypes(Predicate.class, Boolean.TYPE)
        .setParamTypes(PARAMS)
        .setValidReturnType(Boolean.TYPE);

    return MemberChainCompiler.compile(type, tree.ref(), config).map(chain -> {
      return new CompiledRequires(chain, o);
    });
  }
}
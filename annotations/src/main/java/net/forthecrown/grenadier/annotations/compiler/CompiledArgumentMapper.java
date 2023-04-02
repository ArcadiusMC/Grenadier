package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.util.Utils;
import net.forthecrown.grenadier.annotations.tree.MemberChainTree;
import net.forthecrown.grenadier.annotations.tree.MemberChainTree.Kind;

@RequiredArgsConstructor
class CompiledArgumentMapper implements ArgumentModifier<Object, Object> {

  private final Object handle;
  private final MemberChainTree ref;

  @Override
  public Object apply(CommandContext<CommandSource> context, Object input) {
    Object value = handle == null ? input : handle;

    Pair<Object, MemberChainTree> lastPair = ref.resolveLast(value);

    MemberChainTree lastRef = lastPair.right();
    Object lastValue = lastPair.left();

    if (lastRef.kind() == Kind.FIELD) {
      return lastRef.resolve(lastValue);
    }

    Class<?> lastType = lastValue.getClass();
    Method[] methods = lastType.getMethods();

    outer: for (Method m : methods) {
      if (!m.getName().equals(lastRef.name())) {
        continue;
      }

      if (m.getParameterCount() <= 0
          || m.getReturnType() == Void.TYPE
      ) {
        continue;
      }

      Parameter[] params = m.getParameters();
      Object[] invocationMethods = new Object[params.length];

      for (int i = 0; i < m.getParameterCount(); i++) {
        Parameter p = params[i];
        Class<?> pType = p.getType();

        if (CommandSource.class.isAssignableFrom(pType)) {
          invocationMethods[i] = context.getSource();
          continue;
        }

        if (CommandContext.class.isAssignableFrom(pType)) {
          invocationMethods[i] = context;
          continue;
        }

        if (pType.isAssignableFrom(input.getClass())) {
          invocationMethods[i] = input;
          continue;
        }

        continue outer;
        /*throw new IllegalStateException(
            "Unable to determine value for parameter " + i + " (" + p + ")"
                + "\nCommandSource, CommandContext<CommandSource> and input "
                + "parameters are assigned automatically"
        );*/
      }

      try {
        boolean override = m.isAccessible();
        m.setAccessible(true);

        Object result = m.invoke(lastValue, invocationMethods);

        m.setAccessible(override);
        return result;
      } catch (ReflectiveOperationException exc) {
        Utils.sneakyThrow(exc);
        return null;
      }
    }

    throw new IllegalStateException(String.format(
        "Found no methods matching '%s'. Valid methods must have more than 1 "
            + "parameter, not return void and have all parameters be either the "
            + "input value, a CommandSource or a CommandContext<CommandSource>",

        ref.path()
    ));
  }
}
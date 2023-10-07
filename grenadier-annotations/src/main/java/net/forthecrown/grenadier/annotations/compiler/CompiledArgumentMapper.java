package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.ArgumentModifier;
import net.forthecrown.grenadier.annotations.tree.MemberChainTree;
import net.forthecrown.grenadier.annotations.tree.MemberChainTree.Kind;
import net.forthecrown.grenadier.annotations.util.Utils;

@RequiredArgsConstructor
class CompiledArgumentMapper implements ArgumentModifier<Object, Object> {

  private final Object handle;
  private final MemberChainTree ref;

  @Override
  public Object apply(CommandContext<CommandSource> context, Object input)
      throws CommandSyntaxException
  {
    Object value = handle == null ? input : handle;

    Pair<Object, MemberChainTree> lastPair = ref.resolveLast(value);

    MemberChainTree lastRef = lastPair.right();
    Object lastValue = lastPair.left();

    if (lastRef.kind() == Kind.FIELD) {
      Object fieldValue = lastRef.resolve(lastValue);

      if (handle != null && fieldValue instanceof ArgumentModifier modifier) {
        return modifier.apply(context, input);
      }

      return fieldValue;
    }

    Class<?> lastType = lastValue.getClass();
    Method[] methods = lastRef.findMatching(lastType, null)
        .toArray(Method[]::new);

    outer: for (Method m : methods) {
      if (!m.getName().equals(lastRef.name())) {
        continue;
      }

      if (m.getReturnType() == Void.TYPE) {
        continue;
      }

      if (handle != null && m.getParameterCount() < 1) {
        continue;
      }

      Parameter[] params = m.getParameters();
      Object[] invokeParams = new Object[params.length];

      for (int i = 0; i < m.getParameterCount(); i++) {
        Parameter p = params[i];
        Class<?> pType = p.getType();

        if (CommandSource.class == pType) {
          invokeParams[i] = context.getSource();
          continue;
        }

        if (CommandContext.class == pType) {
          invokeParams[i] = context;
          continue;
        }

        if (pType.isInstance(input)) {
          invokeParams[i] = input;
          continue;
        }

        continue outer;
      }

      boolean override = m.isAccessible();
      m.setAccessible(true);

      try {
        return m.invoke(lastValue, invokeParams);
      } catch (ReflectiveOperationException exc) {
        Utils.sneakyThrow(exc);
        return null;
      } finally {
        m.setAccessible(override);
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
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
      Object o = lastRef.resolve(lastValue);

      if (o instanceof ArgumentModifier modifier) {
        return modifier.apply(context, input);
      }

      return o;
    }

    Class<?> lastType = lastValue.getClass();

    Method method = lastRef.findUniqueMethod(lastType).orThrow((pos, msg) -> {
      return new IllegalStateException(String.format(
          "Found no methods matching '%s'. Valid methods must have more than 1 "
              + "parameter, not return void and have all parameters be either the "
              + "input value, a CommandSource or a CommandContext<CommandSource>",

          ref.path()
      ));
    });

    if (method.getReturnType() == Void.class) {
      throw new IllegalStateException(String.format(
          "Method '%s' has illegal return type for argument result mapper (%s)",
          method.getName(),
          method.getReturnType()
      ));
    }


    Parameter[] params = method.getParameters();
    Object[] invocationMethods = new Object[params.length];

    for (int i = 0; i < method.getParameterCount(); i++) {
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

      throw new IllegalStateException(
          "Unable to determine value for parameter " + i + " (" + p + ")"
              + "\nCommandSource, CommandContext<CommandSource> and input "
              + "parameters are assigned automatically"
      );
    }

    boolean override = method.isAccessible();
    method.setAccessible(true);

    try {
      return method.invoke(lastValue, invocationMethods);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return null;
    } finally {
      method.setAccessible(override);
    }
  }
}
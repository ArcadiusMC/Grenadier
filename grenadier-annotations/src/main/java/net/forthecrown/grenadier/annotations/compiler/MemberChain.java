package net.forthecrown.grenadier.annotations.compiler;

import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.forthecrown.grenadier.annotations.util.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * A chain of method calls/field accesses
 */
interface MemberChain {

  /**
   * Gets the next member in the chain of member calls
   * @return Next member chain, or {@code null}, if this is the last node
   */
  MemberChain next();

  Object resolve(Object declaringObject) throws ReflectiveOperationException;

  default Object resolveSafe(Object declaringObject) {
    try {
      return resolve(declaringObject);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return null;
    }
  }

  default @NotNull Pair<Object, MemberChain> resolveLastSafe(Object o) {
    try {
      return resolveLast(o);
    } catch (ReflectiveOperationException exc) {
      Utils.sneakyThrow(exc);
      return null; // Won't happen
    }
  }

  default Pair<Object, MemberChain> resolveLast(Object declaringObject)
      throws ReflectiveOperationException
  {
    if (next() == null) {
      return Pair.of(declaringObject, this);
    }

    Object o = resolve(declaringObject);
    return next().resolveLast(o);
  }

  default MemberChain getLastNode() {
    MemberChain chain = this;

    while (chain.next() != null) {
      chain = chain.next();
    }

    return chain;
  }

  record MethodMember(Method method, boolean override, MemberChain next)
      implements MemberChain
  {

    public Object invokeSafe(Object obj, Object... params) {
      method.setAccessible(true);
      try {
        return method.invoke(obj, params);
      } catch (ReflectiveOperationException exc) {
        Utils.sneakyThrow(exc);
        return null;
      } finally {
        method.setAccessible(override);
      }
    }

    @Override
    public Object resolve(Object declaringObject)
        throws ReflectiveOperationException
    {
      method.setAccessible(true);
      try {
        return method.invoke(declaringObject);
      } finally {
        method.setAccessible(override);
      }
    }
  }

  record FieldMember(Field f, boolean override, MemberChain next)
      implements MemberChain
  {

    @Override
    public Object resolve(Object declaringObject)
        throws ReflectiveOperationException
    {
      f.setAccessible(true);
      try {
        return f.get(declaringObject);
      } finally {
        f.setAccessible(override);
      }
    }
  }
}
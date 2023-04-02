package net.forthecrown.grenadier.annotations.compiler;

import it.unimi.dsi.fastutil.Pair;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.forthecrown.grenadier.annotations.util.Utils;

public interface MemberChain {
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
      try {
        return method.invoke(obj, params);
      } catch (ReflectiveOperationException exc) {
        Utils.sneakyThrow(exc);
        return null;
      }
    }

    @Override
    public Object resolve(Object declaringObject)
        throws ReflectiveOperationException
    {
      method.setAccessible(override);
      Object result = method.invoke(declaringObject);
      method.setAccessible(override);

      return result;
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
      Object result = f.get(declaringObject);
      f.setAccessible(override);

      return result;
    }
  }
}
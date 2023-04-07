package net.forthecrown.grenadier.annotations.util;

import com.destroystokyo.paper.util.SneakyThrow;
import java.lang.reflect.InvocationTargetException;
import net.forthecrown.grenadier.CommandContexts;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class Utils {
  private Utils() {}

  /**
   * Sneakily throws an exception, if the input is a
   * {@link InvocationTargetException} then the cause of the exception is
   * thrown instead
   *
   * @param t Exception to throw
   */
  public static void sneakyThrow(Throwable t) {
    if (t instanceof InvocationTargetException exc) {
      t = exc.getCause();
    }

    SneakyThrow.sneaky(t);
  }

  public static Class<?> primitiveToWrapper(Class<?> c) {
    return CommandContexts.PRIMITIVE_TO_WRAPPER.getOrDefault(c, c);
  }
}
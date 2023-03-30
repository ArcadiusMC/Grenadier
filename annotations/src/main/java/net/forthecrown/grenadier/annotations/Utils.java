package net.forthecrown.grenadier.annotations;

import com.destroystokyo.paper.util.SneakyThrow;
import java.lang.reflect.InvocationTargetException;
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
}
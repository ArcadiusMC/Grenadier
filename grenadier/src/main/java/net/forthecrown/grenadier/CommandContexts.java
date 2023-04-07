package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.StringRange;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for {@link CommandContext} instances.
 *
 */
public final class CommandContexts {
  private CommandContexts() {}

  // 'Map<String, ParsedArgument<?, ?>> arguments' field within
  // CommandContext<S>, lazily initialized when getArguments is called
  private static Field argumentsField;

  /**
   * Map of primitive classes to their Java Object wrappers, for example, this
   * maps a {@code byte} primitive to {@link Byte}.
   * <p>
   * This map is immutable
   */
  public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
      boolean.class,  Boolean.class,
      byte.class,     Byte.class,
      short.class,    Short.class,
      char.class,     Character.class,
      int.class,      Integer.class,
      long.class,     Long.class,
      float.class,    Float.class,
      double.class,   Double.class
  );

  /**
   * Gets the input for a specific node in a command context
   *
   * @param context Context to get input from
   * @param nodeName Name of the node to get input of
   * @return Node input, or {@code null}, if the specified node wasn't found
   *
   * @see #getNodeRange(CommandContext, String)
   */
  public static String getInput(CommandContext<?> context, String nodeName) {
    var range = getNodeRange(context, nodeName);
    return range == null ? null : range.get(context.getInput());
  }

  /**
   * Gets the input range of the specified {@code nodeName} within the specified
   * {@code context}.
   * <p>
   * Loops through all nodes within the context to find one that matches the
   * specified {@code nodeName}
   *
   * @param context Context to get node range from
   * @param nodeName Name of the node to get the range of
   * @return Found range, or {@code null}, if the node wasn't found
   *         in the context
   */
  public static StringRange getNodeRange(CommandContext<?> context,
                                         String nodeName
  ) {
    for (var n: context.getNodes()) {
      var name = n.getNode().getName();
      var range = n.getRange();

      if (!Objects.equals(nodeName, name)) {
        continue;
      }

      int min = range.getStart();
      int max = range.getEnd();

      String input = context.getInput();

      // Crop range to actual size of input, end can
      // sometimes go out of bounds
      return StringRange.between(
          clamp(min, input.length()),
          clamp(max, input.length())
      );
    }

    return null;
  }

  private static int clamp(int v, int max) {
    return v < 0 ? 0 : (v > max ? max : v);
  }

  /**
   * Gets all parsed arguments in the specified {@code context}.
   * <p>
   * Note: This returns the argument map directly, no unmodifiable map layer is
   * applied
   *
   * @param context Context to get arguments from
   * @return Gotten arguments
   *
   * @param <S> Source type
   */
  public static <S> Map<String, ParsedArgument<S, ?>> getArguments(
      CommandContext<S> context
  ) {

    try {
      Field f = getArgumentsField();
      return (Map<String, ParsedArgument<S, ?>>) f.get(context);
    } catch (NoSuchFieldException | IllegalAccessException exc) {
      throw new RuntimeException(exc);
    }
  }

  private static Field getArgumentsField() throws NoSuchFieldException {
    if (argumentsField != null) {
      return argumentsField;
    }

    Class<CommandContext> contextClass = CommandContext.class;
    argumentsField = contextClass.getDeclaredField("arguments");
    argumentsField.setAccessible(true);

    return argumentsField;
  }
}
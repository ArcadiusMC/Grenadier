package net.forthecrown.grenadier.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter annotation for command execution methods.
 *
 * <p>
 * Use example: <pre><code>
 * public void runCommand(@Argument("value") int value) {
 *
 * }
 * </code></pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {

  /**
   * Name of the argument within the {@link com.mojang.brigadier.context.CommandContext}
   * @return Argument name
   */
  String value() default "";

  /**
   * Determines if the argument is required for the command's execution. If set
   * to {@code false}, then the parameter this is annotated with may be null if
   * the command doesn't have the argument
   *
   * @return {@code true} if the argument is optional, {@code false} otherwise
   */
  boolean optional() default false;
}
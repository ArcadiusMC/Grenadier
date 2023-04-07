package net.forthecrown.grenadier.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command data annotation that specifies data about a command
 *
 * @see net.forthecrown.grenadier.annotations
 * Data format documentation
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandData {

  /**
   * Command structure value, see {@link net.forthecrown.grenadier.annotations}
   * for documentation on accepted syntax
   *
   * @return Command data tree
   */
  String value() default "";
}
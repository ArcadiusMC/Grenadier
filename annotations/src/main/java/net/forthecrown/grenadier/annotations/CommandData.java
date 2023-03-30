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

  String value() default "";
}
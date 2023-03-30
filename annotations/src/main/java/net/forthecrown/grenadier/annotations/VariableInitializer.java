package net.forthecrown.grenadier.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks methods that initialize local variables during command
 * parsing and compilation.
 * <p>
 * Use example:
 * <pre><code>
 * &#064;CommandData("""
 * // Data
 * """)
 * public class Example {
 *
 *   &#064;VariableInitializer
 *   void initVariables(Map&lt;String, Object> variables) {
 *     variables.put("var1", "value");
 *   }
 * }
 * </code></pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VariableInitializer {

}
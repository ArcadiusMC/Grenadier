package net.forthecrown.grenadier.annotations;

import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Consumes syntax info from commands registered by
 * {@link AnnotatedCommandContext}
 */
public interface SyntaxConsumer {

  /**
   * Consumes the specified syntax info
   * <p>
   * The {@code argument} parameter is mostly specified by users it does follow
   * a certain formula. Take the following command tree: <pre><code>
   * name = 'command_name'
   * permission = 'permission.name'
   *
   * argument('username', player) {
   *   syntax_label = "&lt;player's name&gt;"
   *
   *   literal('kill') {
   *     requires = permission('permission.player.kill')
   *     description = 'Kills a player'
   *   }
   * }
   * </code></pre>
   * The 'syntax_label' overrides the first argument's label, and the kill
   * literal sets a description, this command will only produce 1 syntax info
   * instance which looks like so:
   * <pre>
   * commandName = 'command_name'
   * argument = 'command_name &lt;player's name&gt; kill'
   * info = 'Kills a player'
   * condition = predicate list [
   *   1) has permission: 'permission.name'
   *   2) has permission: 'permission.player.kill'
   * ]
   * </pre>
   *
   * @param node Grenadier command
   * @param commandClass The object the annotated command tree was declared
   *                     and parsed from
   * @param argument Usage syntax
   * @param info Usage description
   * @param condition A complete condition a source must pass to see the
   *                  specified information
   */
  void accept(GrenadierCommandNode node,
              Object commandClass,
              String argument,
              Component info,
              @Nullable Predicate<CommandSource> condition
  );
}
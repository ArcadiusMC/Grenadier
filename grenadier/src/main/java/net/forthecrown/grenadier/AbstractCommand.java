package net.forthecrown.grenadier;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.permissions.Permission;

/**
 * An abstract class that can be easily extended to create commands.
 *
 * <p>
 * Use example: <pre><code>
 * public class HelloWorldCommand extends AbstractCommand {
 *   public HelloWorldCommand() {
 *     super("hello_world");
 *
 *     // Aliases, description and permission can be define here
 *     setAliases("helloworld");
 *     setDescription("Says hello world");
 *
 *     // Required for the command to be created
 *     register();
 *   }
 *
 *   &#064;Override
 *   public void createCommand(GrenadierCommand command) {
 *     command.execute(context -> {
 *       CommandSource source = context.getSource();
 *
 *       source.sendMessage("Hello, world!");
 *       return 0;
 *     });
 *   }
 * }
 * </code></pre>
 *
 * You can also override the {@link #test(CommandSource)} method to change the
 * requirement for command sources that want to execute this command. Here's an
 * example that only allows a command to be executed in a world named
 * {@code command_world}: <pre><code>
 * public class WorldLimitedCommand extends AbstractCommand {
 *   public WorldLimitedCommand() {
 *     super("world_limited");
 *     register();
 *   }
 *
 *   &#064;Override
 *   public boolean test(CommandSource source) {
 *     World world = source.getWorld();
 *     return world.getName().equals("command_world");
 *   }
 *
 *   &#064;Override
 *   public void createCommand(GrenadierCommand command) {
 *     // ...
 *   }
 * }
 * </code></pre>
 *
 * Note: if you call {@link #setPermission(String)}, then the permission test
 * will always be run, regardless of if you've overridden
 * {@link #test(CommandSource)} or not
 *
 * @see GrenadierCommand
 */
public abstract class AbstractCommand
    extends Nodes
    implements CommandTreeBuilder, Predicate<CommandSource>
{

  private final GrenadierCommand command;
  private boolean registered;

  public AbstractCommand(String name) {
    this.command = Grenadier.createCommand(name);
  }

  public final void register() {
    if (registered) {
      return;
    }

    createCommand(command);
    command.requires(this).register();

    registered = true;
  }

  @Override
  public boolean test(CommandSource source) {
    return true;
  }

  public GrenadierCommand getCommand() {
    return command;
  }

  public String getDescription() {
    return command.getDescription();
  }

  public AbstractCommand setDescription(String description) {
    command.withDescription(description);
    return this;
  }

  public String getPermission() {
    return command.getPermission();
  }

  public AbstractCommand setPermission(String permission) {
    command.withPermission(permission);
    return this;
  }

  public AbstractCommand setPermission(Permission permission) {
    command.withPermission(permission);
    return this;
  }

  public List<String> getAliases() {
    return command.getAliases();
  }

  public AbstractCommand setAliases(String... strings) {
    command.withAliases(strings);
    return this;
  }

  public AbstractCommand setAliases(Collection<String> strings) {
    command.withAliases(strings);
    return this;
  }
}
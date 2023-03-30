package net.forthecrown.grenadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.permissions.Permission;

/**
 * Grenadier command builder
 *
 * <h2>Examples</h2>
 * Let's create a simple command that says "Hello, world" to the sender, here's
 * how that would look like: <pre><code>
 * Grenadier.createCommand("hello_world")
 *
 *     // Set parameters, like aliases, description and permission
 *     .withAliases("helloworld")
 *     .withDescription("Says hello world")
 *     .withPermission("permission.example")
 *
 *     .executes(context -> {
 *       context.getSource().sendMessage("Hello, world!");
 *       return 0;
 *     })
 *
 *     // Registers the command so it can be used ingame
 *     .register();
 * </code></pre>
 *
 * <h3>Using literals</h3>
 * Literals require the player to input a specific text to use the node, as an
 * example, we'll make a command that'll accept the following inputs:
 * <pre>
 * /command
 * /command literal_1
 * </pre>
 *
 * Command tree:
 * <pre><code>
 * Grenadier.createCommand("command")
 *     .executes(context -> {
 *       context.getSource().sendMessage("You entered /command");
 *       return 0;
 *     })
 *
 *     .then(literal("literal_1)
 *       .executes(context -> {
 *         context.getSource().sendMessage("You used the literal");
 *         return 0;
 *       })
 *     )
 *     .register();
 * </code></pre>
 *
 * <h3>Using arguments</h3>
 * Arguments require the player to input a type of value, as an example, we'll
 * make a command that takes in a string and then sends it back to the player.
 * <p>
 * The command will accept these inputs: <pre>
 * /command &lt;string value>
 * </pre>
 *
 * Command tree:
 * <pre><code>
 * Grenadier.createCommand("command")
 *
 *     .then(argument("string value", StringArgumentType.word())
 *       .executes(context -> {
 *         String value = context.getArgument("string value", String.class);
 *
 *         context.getSource().sendMessage("You entered: '" + value + "'");
 *         return 0;
 *       })
 *     )
 *
 *     .register();
 * </code></pre>
 * <p>
 * For an example about a larger and more complex command structure, see
 * <a href="https://github.com/ForTheCrown/FTC/blob/main/src/main/java/net/forthecrown/commands/admin/CommandInvStore.java">this command, from the FTC server plugin</a>
 */
public class GrenadierCommand extends LiteralArgumentBuilder<CommandSource> {

  private final List<String> aliases = new ArrayList<>();
  private String description;

  private String permission;

  public GrenadierCommand(String literal) {
    super(literal.toLowerCase());
    Grenadier.ensureValidLabel(literal);
  }

  public String getDescription() {
    return description;
  }

  public GrenadierCommand withDescription(String description) {
    this.description = description;
    return this;
  }

  public String getPermission() {
    return permission;
  }

  public GrenadierCommand withPermission(String permission) {
    this.permission = permission;
    return this;
  }

  public GrenadierCommand withPermission(Permission permission) {
    this.permission = permission == null ? null : permission.getName();
    return this;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public GrenadierCommand withAliases(Collection<String> aliases) {
    aliases.forEach(Grenadier::ensureValidLabel);

    this.aliases.clear();
    this.aliases.addAll(aliases);
    return this;
  }

  public GrenadierCommand withAliases(String... aliases) {
    return withAliases(List.of(aliases));
  }

  @Override
  protected GrenadierCommand getThis() {
    return this;
  }

  @Override
  public GrenadierCommand then(CommandNode<CommandSource> argument) {
    return (GrenadierCommand) super.then(argument);
  }

  @Override
  public GrenadierCommand then(ArgumentBuilder<CommandSource, ?> argument) {
    return (GrenadierCommand) super.then(argument);
  }

  @Override
  public GrenadierCommand executes(Command<CommandSource> command) {
    return (GrenadierCommand) super.executes(command);
  }

  @Override
  public GrenadierCommand requires(Predicate<CommandSource> requirement) {
    return (GrenadierCommand) super.requires(requirement);
  }

  @Override
  public GrenadierCommand redirect(CommandNode<CommandSource> target) {
    return (GrenadierCommand) super.redirect(target);
  }

  @Override
  public GrenadierCommand redirect(
      CommandNode<CommandSource> target,
      SingleRedirectModifier<CommandSource> modifier
  ) {
    return (GrenadierCommand) super.redirect(target, modifier);
  }

  @Override
  public GrenadierCommand fork(
      CommandNode<CommandSource> target,
      RedirectModifier<CommandSource> modifier
  ) {
    return (GrenadierCommand) super.fork(target, modifier);
  }

  @Override
  public GrenadierCommand forward(
      CommandNode<CommandSource> target,
      RedirectModifier<CommandSource> modifier,
      boolean fork
  ) {
    return (GrenadierCommand) super.forward(target, modifier, fork);
  }

  @Override
  public GrenadierCommandNode build() {
    GrenadierCommandNode result = new GrenadierCommandNode(
        getLiteral(),
        getCommand(),
        getRequirement(),
        getRedirect(),
        getRedirectModifier(),
        isFork(),
        permission,
        description,
        aliases
    );

    for (CommandNode<CommandSource> argument : getArguments()) {
      result.addChild(argument);
    }

    return result;
  }

  public GrenadierCommandNode register() {
    var built = build();
    Grenadier.dispatcher().getRoot().addChild(built);
    return built;
  }
}
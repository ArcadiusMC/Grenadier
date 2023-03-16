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

public class GrenadierCommand extends LiteralArgumentBuilder<CommandSource> {

  private final List<String> aliases = new ArrayList<>();
  private String description;

  private String permission;

  public GrenadierCommand(String literal) {
    super(literal.toLowerCase());
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
      RedirectModifier<CommandSource> modifier, boolean fork
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
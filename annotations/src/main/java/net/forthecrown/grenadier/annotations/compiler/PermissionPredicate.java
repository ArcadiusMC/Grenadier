package net.forthecrown.grenadier.annotations.compiler;

import java.util.Objects;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;

class PermissionPredicate implements Predicate<CommandSource> {

  private final String permission;

  public PermissionPredicate(String permission) {
    Objects.requireNonNull(permission);
    this.permission = permission;
  }

  @Override
  public boolean test(CommandSource source) {
    return source.hasPermission(permission);
  }
}
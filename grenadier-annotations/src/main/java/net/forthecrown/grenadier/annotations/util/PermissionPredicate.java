package net.forthecrown.grenadier.annotations.util;

import java.util.Objects;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;

public class PermissionPredicate implements Predicate<CommandSource> {

  private final String permission;

  public PermissionPredicate(String permission) {
    Objects.requireNonNull(permission);
    this.permission = permission;
  }

  public String getPermission() {
    return permission;
  }

  @Override
  public boolean test(CommandSource source) {
    return source.hasPermission(permission);
  }
}
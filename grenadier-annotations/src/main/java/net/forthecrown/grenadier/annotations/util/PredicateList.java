package net.forthecrown.grenadier.annotations.util;

import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;

public class PredicateList implements Predicate<CommandSource> {

  private final Predicate<CommandSource>[] predicates;

  public PredicateList(Predicate<CommandSource>[] predicates) {
    this.predicates = predicates;
  }

  public Predicate<CommandSource>[] getPredicates() {
    return predicates;
  }

  @Override
  public boolean test(CommandSource source) {
    for (Predicate<CommandSource> predicate : predicates) {
      if (!predicate.test(source)) {
        return false;
      }
    }

    return true;
  }
}
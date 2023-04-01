package net.forthecrown.grenadier.annotations.compiler;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.util.Utils;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef;

@RequiredArgsConstructor
class CompiledRequires implements Predicate<CommandSource> {

  static final Class<?>[] PARAMS = {CommandSource.class};

  private final ClassComponentRef ref;
  private final Object commandClass;

  @Override
  public boolean test(CommandSource source) {
    try {
      return ref.execute(
          Boolean.TYPE,
          Predicate.class,
          predicate -> predicate.test(source),
          PARAMS,
          commandClass,
          source
      );
    } catch (CommandSyntaxException exc) {
      Utils.sneakyThrow(exc);
      return false;
    }
  }
}
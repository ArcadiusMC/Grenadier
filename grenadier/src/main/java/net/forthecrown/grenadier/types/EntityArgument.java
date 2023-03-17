package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;

/**
 * Parses an entity selector
 * <p>
 * Input examples: <pre>
 * PlayerName
 * 21290ce5-679c-4917-b30e-168c0d450c72
 * &#064;e[type=minecraft:spider,limit=1,distance=..10]
 * </pre>
 */
public interface EntityArgument extends ArgumentType<EntitySelector> {

  /**
   * Gets whether the resulting selector is allowed to select more than 1 entity
   * @return {@code true}, if this argument may select more than 1 entity,
   *         {@code false} otherwise
   */
  boolean allowsMultiple();

  /**
   * Gets whether the resulting selector is allowed to include entities or is
   * limited to just players
   *
   * @return {@code true}, if all entities not just players can be selected,
   *         {@code false} otherwise
   */
  boolean includesEntities();

  @Override
  default EntitySelector parse(StringReader reader)
      throws CommandSyntaxException
  {
    return parse(reader, false);
  }

  EntitySelector parse(StringReader reader, boolean overridePermissions)
      throws CommandSyntaxException;


  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );
}
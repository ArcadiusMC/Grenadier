package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Argument type that parses a list of specific types.
 * <p>
 * The inputted list expects a format in which each entry is separated by
 * commas
 *
 * <h2>Usage example</h2>
 * As an example, let's say we want to parse a list of
 * {@link ItemArgument.Result}, we would do this by first creating the array
 * argument like so: <pre><code>
 * static final ArrayArgument&lt;ItemArgument.Result> ARRAY
 *     = ArgumentTypes.array(ArgumentTypes.item());
 * </code></pre>
 *
 * Then, during command execution, we can access the parsed result like so: <pre><code>
 * CommandContext&lt;CommandSource> context = // ...
 *
 * List&lt;ItemArgument.Result> results
 *     = context.getArgument("argument name", List.class);
 * </code></pre>
 *
 *
 * @param <T> List type
 */
public interface ArrayArgument<T> extends ArgumentType<List<T>> {

  /**
   * Gets the base argument type
   * @return List entry argument type
   */
  ArgumentType<T> listType();

  @Override
  List<T> parse(StringReader reader) throws CommandSyntaxException;

  @Override
  <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  );
}
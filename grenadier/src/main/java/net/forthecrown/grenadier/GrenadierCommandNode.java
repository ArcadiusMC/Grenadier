package net.forthecrown.grenadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GrenadierCommandNode extends LiteralCommandNode<CommandSource> {

  private final String permission;
  private final String description;

  private final List<String> aliases;

  public GrenadierCommandNode(String literal,
                              Command<CommandSource> command,
                              Predicate<CommandSource> requirement,
                              CommandNode<CommandSource> redirect,
                              RedirectModifier<CommandSource> modifier,
                              boolean forks,
                              String permission,
                              String description,
                              List<String> aliases
  ) {
    super(literal, command, requirement, redirect, modifier, forks);

    this.permission = permission;
    this.description = description;

    this.aliases = aliases.stream().map(String::toLowerCase).toList();
  }

  public String getPermission() {
    return permission;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public void forEachLabel(Consumer<String> consumer) {
    labels().forEachRemaining(consumer);
  }

  public Iterator<String> labels() {
    return new Iterator<>() {
      final Iterator<String> aliasIterator = aliases.iterator();
      String next = getLiteral();

      @Override
      public boolean hasNext() {
        if (next != null) {
          return true;
        }

        return aliasIterator.hasNext();
      }

      @Override
      public String next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        if (next != null) {
          var str = next;
          next = null;
          return str;
        }

        return aliasIterator.next();
      }
    };
  }

  public int parse(StringReader reader) {
    var it = labels();

    while (it.hasNext()) {
      var label = it.next();

      if (Readers.startsWithIgnoreCase(reader, label)) {
        int end = reader.getCursor() + label.length();
        reader.setCursor(end);
        return end;
      }
    }

    return -1;
  }

  @Override
  public void parse(StringReader reader,
                    CommandContextBuilder<CommandSource> contextBuilder
  ) throws CommandSyntaxException {
    final int start = reader.getCursor();
    final int end = parse(reader);

    if (end > -1) {
      contextBuilder.withNode(this, StringRange.between(start, end));
      return;
    }

    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
        .literalIncorrect()
        .createWithContext(reader, getAliases());
  }

  @Override
  public CompletableFuture<Suggestions> listSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder, this::labels);
  }

  @Override
  public boolean isValidInput(String input) {
    return aliases.contains(input.toLowerCase())
        && getLiteral().equalsIgnoreCase(input);
  }

  @Override
  public synchronized boolean canUse(CommandSource source) {
    source.setCurrentNode(this);

    if (permission != null && !source.hasPermission(permission)) {
      return false;
    }

    return super.canUse(source);
  }
}
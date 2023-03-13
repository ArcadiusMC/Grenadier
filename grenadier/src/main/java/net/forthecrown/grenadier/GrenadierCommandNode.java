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
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.forthecrown.grenadier.utils.Readers;

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

  public Iterator<String> labels() {
    return new Iterator<>() {
      final Iterator<String> aliasIterator = aliases.iterator();
      String next = getLiteral();

      @Override
      public boolean hasNext() {
        if (next != null) {
          return false;
        }

        return aliasIterator.hasNext();
      }

      @Override
      public String next() {
        if (next != null) {
          var str = next;
          next = null;
          return str;
        }

        return aliasIterator.next();
      }
    };
  }

  private int parse(StringReader reader) {
    var it = labels();

    while (it.hasNext()) {
      var label = it.next();

      if (Readers.startsWithIgnoreCase(reader, label)) {
        return reader.getCursor() + label.length();
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
      reader.setCursor(end);
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
    if (permission != null && !source.hasPermission(permission)) {
      return false;
    }

    return super.canUse(source);
  }
}
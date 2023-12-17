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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.Plugin;

public class GrenadierCommandNode extends LiteralCommandNode<CommandSource> {

  private final Plugin plugin;

  private final String permission;
  private final Component description;

  private final List<String> aliases;

  private final boolean plainTranslation;

  public GrenadierCommandNode(
      String literal,
      Command<CommandSource> command,
      Predicate<CommandSource> requirement,
      CommandNode<CommandSource> redirect,
      RedirectModifier<CommandSource> modifier,
      boolean forks,
      String permission,
      Component description,
      List<String> aliases,
      boolean plainTranslation,
      Plugin plugin
  ) {
    super(literal, command, requirement, redirect, modifier, forks);

    this.permission = permission;
    this.description = description;
    this.plainTranslation = plainTranslation;
    this.plugin = plugin;

    this.aliases = aliases.stream().map(String::toLowerCase).toList();
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public boolean isPlainTranslation() {
    return plainTranslation;
  }

  public String getPermission() {
    return permission;
  }

  public String getDescription() {
    return description == null
        ? null
        : LegacyComponentSerializer.legacySection().serialize(description);
  }

  @Override
  public GrenadierCommand createBuilder() {
    var builder = Grenadier.createCommand(getName());
    builder.withAliases(getAliases());
    builder.withDescription(description);
    builder.withPermission(getPermission());
    builder.requires(getRequirement());
    builder.forward(getRedirect(), getRedirectModifier(), isFork());

    if (getCommand() != null) {
      builder.executes(getCommand());
    }

    return builder;
  }

  public Component description() {
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

    // Input may contain fallback prefix, especially if executed from Bukkit
    Readers.skipIrrelevantInput(reader);

    while (it.hasNext()) {
      var label = it.next();

      if (Readers.startsWithArgument(reader, label)) {
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
    return parse(new StringReader(input)) > -1;
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
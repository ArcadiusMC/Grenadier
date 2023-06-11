package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.GrenadierCommandNode;
import net.forthecrown.grenadier.Readers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

class TreeTranslator {

  public static final Command<CommandSourceStack> COMMAND = context -> {
    StringReader input = Readers.fromContextInput(context.getLastChild());
    CommandSource source = InternalUtil.wrap(context.getSource());

    return InternalUtil.execute(source, input);
  };

  public static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (context, builder) -> {
    CommandSource source = InternalUtil.wrap(context.getSource());
    StringReader reader = Readers.fromContextInput(context.getLastChild());

    CommandDispatcher<CommandSource> dispatcher = Grenadier.dispatcher();

    try {
      ParseResults<CommandSource> parseResults = dispatcher.parse(reader, source);
      return dispatcher.getCompletionSuggestions(parseResults);
    } catch (Throwable t) {
      Grenadier.getProvider()
          .getExceptionHandler()
          .onSuggestionException(reader.getString(), t, source);

      return Suggestions.empty();
    }
  };

  public static List<CommandNode<CommandSourceStack>> translate(
      CommandNode<CommandSource> node,
      GrenadierCommandNode root
  ) {
    if (node instanceof GrenadierCommandNode grenadierNode) {
      return translateGrenadier(grenadierNode, root);
    }

    if (node instanceof LiteralCommandNode<CommandSource> literal) {
      return Collections.singletonList(translateLiteral(literal, root));
    }

    if (node instanceof ArgumentCommandNode<CommandSource, ?> argument) {
      return Collections.singletonList(translateRequired(argument, root));
    }

    throw new IllegalArgumentException("Unknown node type: " + node);
  }

  public static List<CommandNode<CommandSourceStack>> translateGrenadier(
      GrenadierCommandNode grenadierNode,
      GrenadierCommandNode root
  ) {
    List<CommandNode<CommandSourceStack>> results = new ArrayList<>();

    LiteralCommandNode<CommandSourceStack> translated
        = translateLiteral(grenadierNode, root);

    grenadierNode.forEachLabel(s -> {
      results.add(GrenadierCommandData.withLabel(translated, s));
    });

    return results;
  }

  public static LiteralCommandNode<CommandSourceStack> translateLiteral(
      LiteralCommandNode<CommandSource> node,
      GrenadierCommandNode root
  ) {
    LiteralArgumentBuilder<CommandSourceStack> builder
        = LiteralArgumentBuilder.literal(node.getLiteral());

    return (LiteralCommandNode<CommandSourceStack>)
        translateBase(builder, node, root);
  }

  public static CommandNode<CommandSourceStack> translateRequired(
      ArgumentCommandNode<CommandSource, ?> node,
      GrenadierCommandNode root
  ) {
    ArgumentType<?> type = translateType(node.getType());
    boolean useVanillaSuggestions = useVanillaSuggestions(node.getType());

    RequiredArgumentBuilder<CommandSourceStack, ?> builder
        = RequiredArgumentBuilder.argument(node.getName(), type);

    if (!useVanillaSuggestions) {
      builder.suggests(translateSuggestions(node, root));
    }

    return translateBase(builder, node, root);
  }

  private static CommandNode<CommandSourceStack> translateBase(
      ArgumentBuilder<CommandSourceStack, ?> result,
      CommandNode<CommandSource> grenadierNode,
      GrenadierCommandNode root
  ) {
    result.executes(translateCommand(grenadierNode.getCommand()))
        .requires(translateTest(grenadierNode, root));

    if (grenadierNode.getRedirect() != null) {
      RequiredArgumentBuilder<CommandSourceStack, String> builder
          = RequiredArgumentBuilder.argument(
              grenadierNode.getName(),
              StringArgumentType.greedyString()
          );

      builder.executes(COMMAND).suggests(SUGGESTION_PROVIDER);

      return result.then(builder).build();
    }

    Collection<CommandNode<CommandSource>> children
        = grenadierNode.getChildren();

    for (var c: children) {
      List<CommandNode<CommandSourceStack>> translated = translate(c, root);
      translated.forEach(result::then);
    }

    return result.build();
  }

  private static Predicate<CommandSourceStack> translateTest(
      CommandNode<CommandSource> node,
      GrenadierCommandNode root
  ) {
    return stack -> {
      CommandSource source = InternalUtil.wrap(stack);
      source.setCurrentNode(root);
      return node.canUse(source);
    };
  }

  private static Command<CommandSourceStack> translateCommand(
      Command<CommandSource> command
  ) {
    if (command == null) {
      return null;
    }

    return COMMAND;
  }

  private static SuggestionProvider<CommandSourceStack> translateSuggestions(
      ArgumentCommandNode<CommandSource, ?> grenadierNode,
      GrenadierCommandNode root
  ) {
    return (context, builder) -> {
      StringReader reader = Readers.createFiltered(context.getInput());
      CommandSource source = InternalUtil.wrap(context.getSource());
      source.setCurrentNode(root);

      CommandDispatcher<CommandSource> dispatcher = Grenadier.dispatcher();

      ParseResults<CommandSource> parseResults
          = dispatcher.parse(reader, source);

      CommandContext<CommandSource> grenadierContext
          = parseResults.getContext()
          .build(context.getInput())
          .getLastChild();

      for (var p: grenadierContext.getNodes()) {
        var node = p.getNode();

        if (!node.canUse(source)) {
          return Suggestions.empty();
        }
      }

      try {
        return grenadierNode.listSuggestions(grenadierContext, builder);
      } catch (CommandSyntaxException exc) {
        return builder.buildFuture();
      } catch (Throwable t) {
        Grenadier.getProvider()
            .getExceptionHandler()
            .onSuggestionException(context.getInput(), t, source);

        return Suggestions.empty();
      }
    };
  }

  /* --------------------- ARGUMENT TYPE TRANSLATION ---------------------- */

  private static boolean useVanillaSuggestions(ArgumentType<?> type) {
    if (type instanceof VanillaMappedArgument vanilla) {
      return vanilla.useVanillaSuggestions();
    }

    return false;
  }

  private static ArgumentType<?> translateType(ArgumentType<?> type) {
    if (ArgumentTypeInfos.isClassRecognized(type.getClass())) {
      return type;
    }

    if (type instanceof VanillaMappedArgument vanilla) {
      var vanillaType = vanilla.getVanillaType(InternalUtil.CONTEXT);
      Objects.requireNonNull(vanillaType, "getVanillaType returned null");

      if (!ArgumentTypeInfos.isClassRecognized(vanillaType.getClass())) {
        throw new IllegalArgumentException(
            String.format(
                "getVanillaType returned a non-vanilla argument type: %s",
                vanillaType
            )
        );
      }

      return vanillaType;
    }

    return GameProfileArgument.gameProfile();
  }

}
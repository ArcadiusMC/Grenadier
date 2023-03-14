package net.forthecrown.grenadier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

/**
 * Methods to handle and format {@link CommandSyntaxException} instances.
 *
 * @see #formatExceptionContext(CommandSyntaxException)
 * Exception formatting
 *
 * @see #handle(CommandSyntaxException, CommandSource)
 * Exception handling
 */
public final class SyntaxExceptions {
  private SyntaxExceptions() {}

  /**
   * The default style of the error messages, red by default
   */
  public static Style ERROR_MESSAGE_STYLE
      = Style.style(NamedTextColor.RED);

  /**
   * The style of the text in the context, gray by default
   */
  public static Style GRAY_CONTEXT_STYLE
      = Style.style(NamedTextColor.GRAY);

  /**
   * The style of the part of the context where you went wrong, underlined red
   * by default
   */
  public static Style RED_CONTEXT_STYLE
      = Style.style(NamedTextColor.RED, TextDecoration.UNDERLINED);

  /**
   * The style of the <--[HERE] pointer, italic red by default
   */
  public static Style HERE_POINTER_STYLE
      = Style.style(NamedTextColor.RED, TextDecoration.ITALIC);

  /**
   * Handles a syntax exception
   * <p>
   * Uses the {@link #formatExceptionContext(CommandSyntaxException)} to format
   * the exception and then uses {@link CommandSource#sendFailure(Component)}
   * to send the command source the error message
   *
   * @param exception Syntax exception
   * @param source Command source
   */
  public static void handle(CommandSyntaxException exception,
                            CommandSource source
  ) {
    var message = formatCommandException(exception);
    source.sendFailure(message);
  }

  /**
   * Formats a command exception into a single formatted text.
   * <p>
   * Uses {@link SyntaxExceptions#ERROR_MESSAGE_STYLE} as the style for the
   * error message.
   * <p>
   * If the given exception has a context, it calls
   * {@link #formatExceptionContext(CommandSyntaxException)} to format it and
   * appends it onto the result on a second line.
   *
   * @param exception The exception to format
   * @return The formatted exception
   * @see #formatExceptionContext(CommandSyntaxException)
   */
  public static Component formatCommandException(
      CommandSyntaxException exception
  ) {
    Component initialMessage
        = exception.componentMessage().style(ERROR_MESSAGE_STYLE);

    if (exception.getInput() == null || exception.getCursor() < 0) {
      return initialMessage;
    }

    return text()
        .append(initialMessage)
        .append(Component.newline())
        .append(formatExceptionContext(exception))
        .build();
  }

  /**
   * Formats a command syntax exception's context into a single text.
   * <p>
   * If the given exception's context is null or cursor at -1, then null is
   * returned.
   *
   * @param e The exception to format the context of
   * @return The formatted context, or null, if there was no context to format
   */
  public static @Nullable Component formatExceptionContext(
      CommandSyntaxException e
  ) {
    if (e.getInput() == null || e.getCursor() < 0) {
      return null;
    }

    final TextComponent.Builder builder = text();
    final int cursor = Math.min(e.getInput().length(), e.getCursor());

    //Either start of input or cursor - 10
    final int start = Math.max(0,
        cursor - CommandSyntaxException.CONTEXT_AMOUNT);

    //Context too long, add dots
    if (start != 0) {
      builder.append(text("...", GRAY_CONTEXT_STYLE));
    }

    String grayContext = e.getInput().substring(start, cursor);
    String redContext = e.getInput().substring(cursor);

    builder.append(
        text()
            //Clicking on the exception will put the input in chat
            .clickEvent(ClickEvent.suggestCommand("/" + e.getInput()))

            // Show command in hover event
            .hoverEvent(
                text()
                    .append(
                        text("/" + e.getInput().substring(0, cursor),
                            GRAY_CONTEXT_STYLE
                        ),
                        text(redContext, RED_CONTEXT_STYLE)
                    )
                    .build()
            )

            .append(
                text(grayContext, GRAY_CONTEXT_STYLE),
                text(redContext, RED_CONTEXT_STYLE)
            )

            .append(translatable("command.context.here", HERE_POINTER_STYLE))

            .build()
    );

    return builder.build();
  }
}
package net.forthecrown.grenadier.internal;

import static com.mojang.brigadier.exceptions.CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.mojang.brigadier.StringReader;
import net.forthecrown.grenadier.CommandExceptionHandler;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Grenadier;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

class DefaultExceptionHandler implements CommandExceptionHandler {

  private static final Logger LOGGER = Grenadier.getLogger();

  @Override
  public void onCommandException(
      StringReader input,
      Throwable throwable,
      CommandSource source
  ) {
    LOGGER.error("Error running command '{}'", input.getString(), throwable);

    TranslatableComponent.Builder builder = translatable()
        .key("command.failed")
        .color(NamedTextColor.RED);

    TextComponent.Builder hoverBuilder = text();
    hoverBuilder.append(text(throwable.toString()));

    if (ENABLE_COMMAND_STACK_TRACES || LOGGER.isDebugEnabled()) {
      for (var element: throwable.getStackTrace()) {
        String line = element.getClassName()
            + "#" + element.getMethodName()
            + ":" + element.getLineNumber();

        hoverBuilder.append(
            newline(),
            text(line)
        );
      }
    }

    builder.hoverEvent(hoverBuilder.build());
    source.sendFailure(builder.build());
  }

  @Override
  public void onSuggestionException(
      String input,
      Throwable throwable,
      CommandSource source
  ) {
    LOGGER.error("Error getting suggestions for '{}'", input, throwable);
  }
}
package net.forthecrown.grenadier;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

@CommandData("file = signedit.gcn")
public class SignEditCommand {

  public static final int LINES = 4;

  public static final String SIGN_ARG =  "pos";
  public static final String LINE_ARG = "line";
  public static final String TEXT_ARG = "text";
  public static final String GLOW_ARG = "glow";

  private final Map<UUID, Component[]> copiedSignLines = new HashMap<>();

  public SignEditCommand() {

  }

  @VariableInitializer
  public void initializeLocalVariables(Map<String, Object> variables) {
    variables.put("pos", SIGN_ARG);
  }

  /* ------------------------- ARGUMENT MAPPERS --------------------------- */

  // Maps parsed position to sign
  public Sign positionToSign(CommandSource source, ParsedPosition position)
      throws CommandSyntaxException
  {
    Location location = position.apply(source);
    Block block = location.getBlock();
    BlockState state = block.getState();

    if (state instanceof Sign sign) {
      return sign;
    }

    throw Grenadier.exceptions().create("Block at %sx %sy %sz is not a sign",
        location.getBlockX(),
        location.getBlockY(),
        location.getBlockZ()
    );
  }

  public Component stringToComponent(String value) {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(value);
  }

  /* -------------------------- EXECUTES METHODS -------------------------- */

  public void clear(CommandSource source, @Argument(SIGN_ARG) Sign sign)
      throws CommandSyntaxException
  {
    for (int i = 0; i < LINES; i++) {
      sign.line(i, empty());
    }

    attemptSignUpdate(sign, source, () -> text("Cleared sign"));
  }

  public void copy(CommandSource source, @Argument(SIGN_ARG) Sign sign)
      throws CommandSyntaxException
  {
    Player player = source.asPlayer();
    Component[] lines = new Component[LINES];

    for (int i = 0; i < LINES; i++) {
      lines[i] = sign.line(i);
    }

    copiedSignLines.put(player.getUniqueId(), lines);
    source.sendSuccess(text("Copied sign"));
  }

  public void paste(CommandSource source, @Argument(SIGN_ARG) Sign sign)
      throws CommandSyntaxException
  {
    Player player = source.asPlayer();
    Component[] lines = copiedSignLines.get(player.getUniqueId());

    if (lines == null) {
      throw Grenadier.exceptions().create("No copied sign");
    }

    for (int i = 0; i < LINES; i++) {
      sign.line(i, lines[i]);
    }

    attemptSignUpdate(sign, source, () -> text("Pasted sign"));
  }

  public void setGlowing(CommandSource source,
                         @Argument(SIGN_ARG) Sign sign,
                         @Argument(GLOW_ARG) boolean glowing
  ) throws CommandSyntaxException {
    sign.setGlowingText(glowing);

    attemptSignUpdate(sign, source, () -> {
      if (glowing) {
        return text("Made sign glow");
      } else {
        return text("Made sign not glow");
      }
    });
  }

  public void setLine(CommandSource source,
                      @Argument(SIGN_ARG) Sign sign,
                      @Argument(LINE_ARG) int line,
                      @Argument(TEXT_ARG) Component text
  ) throws CommandSyntaxException {
    sign.line(line - 1, text);

    attemptSignUpdate(sign, source, () -> {
      return text("Set line ")
          .append(text(line))
          .append(text(" to "))
          .append(text);
    });
  }

  public void clearLine(CommandSource source,
                        @Argument(SIGN_ARG) Sign sign,
                        @Argument(LINE_ARG) int line
  ) throws CommandSyntaxException {
    Component existingText = sign.line(line - 1);
    sign.line(line-1, empty());

    attemptSignUpdate(sign, source, () -> {
      return text("Cleared line ")
          .append(text(line))
          .append(text(", text: '"))
          .append(existingText)
          .append(text("'"));
    });
  }


  private void attemptSignUpdate(Sign sign,
                                 CommandSource source,
                                 Supplier<Component> message
  ) throws CommandSyntaxException {
    if (sign.update(false)) {
      source.sendSuccess(message.get());
      return;
    }

    throw Grenadier.exceptions().create("Failed to update sign");
  }

  /* ---------------------------- SUGGESTIONS ----------------------------- */

  public CompletableFuture<Suggestions> suggestSignLine(
      CommandSource source,
      SuggestionsBuilder builder,
      @Argument(SIGN_ARG) Sign sign
  ) {
    return Completions.suggest(builder,
        sign.lines()
            .stream()
            .map(LegacyComponentSerializer.legacyAmpersand()::serialize)
    );
  }
}
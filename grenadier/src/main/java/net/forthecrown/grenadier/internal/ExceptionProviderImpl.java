package net.forthecrown.grenadier.internal;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.util.Arrays;
import net.forthecrown.grenadier.ExceptionProvider;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.nbt.path.PathParseException;
import net.forthecrown.nbt.string.TagParseException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.KeybindComponent.KeybindLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

class ExceptionProviderImpl implements ExceptionProvider {

  private static final LegacyComponentSerializer LEGACY
      = LegacyComponentSerializer.builder()
      .extractUrls()
      .hexColors()
      .build();

  /* -------------------------- FACTORY METHODS --------------------------- */

  @Override
  public CommandSyntaxException create(String message, Object... args) {
    return create(LEGACY.deserialize(message.formatted(args)));
  }

  @Override
  public CommandSyntaxException create(Component message) {
    return new CommandSyntaxException(this, Grenadier.toMessage(message));
  }

  @Override
  public CommandSyntaxException createWithContext(String message,
                                                  ImmutableStringReader reader,
                                                  Object... args
  ) {
    return createWithContext(
        LEGACY.deserialize(message.formatted(args)),
        reader
    );
  }

  @Override
  public CommandSyntaxException createWithContext(Component message,
                                                  ImmutableStringReader reader
  ) {
    return new CommandSyntaxException(
        this,
        Grenadier.toMessage(message),
        reader.getString(), reader.getCursor()
    );
  }

  private static Component textValueOf(Object o) {
    if (o instanceof ComponentLike like) {
      return like.asComponent();
    }

    if (o instanceof Translatable translatable) {
      return Component.translatable(translatable);
    }

    if (o instanceof KeybindLike like) {
      return Component.keybind(like);
    }

    return LEGACY.deserialize(String.valueOf(o));
  }

  private CommandSyntaxException translatable(String format, Object... args) {
    Component message = Component.translatable(
        format,
        Arrays.stream(args)
            .map(ExceptionProviderImpl::textValueOf)
            .toList()
    );

    return create(message);
  }

  private CommandSyntaxException translatableWithContext(
      String format,
      ImmutableStringReader reader,
      Object... args
  ) {
    Component message = Component.translatable(
        format,
        Arrays.stream(args)
            .map(ExceptionProviderImpl::textValueOf)
            .toList()
    );

    return createWithContext(message, reader);
  }

  /* -------------------- ARGUMENT SPECIFIC EXCEPTIONS -------------------- */

  @Override
  public CommandSyntaxException overstacked(Material material) {
    return translatable(
        "arguments.items.overstacked",
        material, material.getMaxStackSize()
    );
  }

  @Override
  public CommandSyntaxException unknownWorld(String name, StringReader reader) {
    return translatableWithContext("argument.dimension.invalid", reader, name);
  }

  @Override
  public CommandSyntaxException unknownMapValue(String word,
                                                StringReader reader
  ) {
    return translatableWithContext("argument.enum.invalid", reader, word);
  }

  @Override
  public CommandSyntaxException invalidKey(String name, StringReader reader) {
    return translatableWithContext("argument.id.invalid", reader, name);
  }

  @Override
  public <E extends Enum<E>> CommandSyntaxException invalidEnum(
      Class<E> enumType,
      String word,
      StringReader reader
  ) {
    return translatableWithContext("argument.enum.invalid", reader, word);
  }

  @Override
  public CommandSyntaxException unknownResource(NamespacedKey key,
                                                String registryName,
                                                StringReader reader
  ) {
    return translatableWithContext(
        "argument.resource.not_found",
        reader,
        key, registryName
    );
  }

  @Override
  public CommandSyntaxException pathParseException(PathParseException exc,
                                                   StringReader reader
  ) {
    if (exc.getCause() instanceof IOException) {
      return createWithContext(exc.getCause().getMessage(), reader);
    }

    return createWithContext(exc.getMessage(), reader);
  }

  @Override
  public CommandSyntaxException tagParseException(TagParseException exc,
                                                  StringReader reader
  ) {
    if (exc.getCause() instanceof IOException) {
      return createWithContext(exc.getCause().getMessage(), reader);
    }

    return createWithContext(exc.getMessage(), reader);
  }

  @Override
  public CommandSyntaxException unknownGamemode(String word,
                                                StringReader reader
  ) {
    return translatableWithContext("argument.gamemode.invalid", reader, word);
  }

  @Override
  public CommandSyntaxException invalidTimeUnit(String word,
                                                StringReader reader
  ) {
    return translatableWithContext("argument.time.invalid_unit", reader);
  }

  @Override
  public CommandSyntaxException unknownObjective(String name,
                                                 StringReader reader
  ) {
    return translatableWithContext("arguments.objective.notFound",
        reader, name
    );
  }

  @Override
  public CommandSyntaxException unknownTeam(String word, StringReader reader) {
    return translatableWithContext("team.notFound", reader, word);
  }

  @Override
  public <T extends CommandSender> CommandSyntaxException sourceMustBe(
      Class<T> clazz
  ) {
    if (Player.class.isAssignableFrom(clazz)) {
      return translatable("permissions.requires.player");
    }

    if (Entity.class.isAssignableFrom(clazz)) {
      return translatable("permissions.requires.entity");
    }

    return create("Only %ss can run this command", clazz.getSimpleName());
  }

  @Override
  public CommandSyntaxException selectorOnlyOnePlayer(StringReader reader) {
    return translatableWithContext("argument.player.toomany", reader);
  }

  @Override
  public CommandSyntaxException selectorOnlyOneEntity(StringReader reader) {
    return translatableWithContext("argument.entity.toomany", reader);
  }

  @Override
  public CommandSyntaxException selectorOnlyPlayersAllowed(StringReader reader) {
    return translatableWithContext("argument.player.entities", reader);
  }

  @Override
  public CommandSyntaxException noPlayerFound() {
    return translatable("argument.entity.notfound.player");
  }

  @Override
  public CommandSyntaxException noEntityFound() {
    return translatable("argument.entity.notfound.entity");
  }

  @Override
  public CommandSyntaxException unknownLootTable(NamespacedKey key,
                                                 StringReader reader
  ) {
    return unknownResource(key, "LootTable", reader);
  }

  @Override
  public CommandSyntaxException posNotComplete(StringReader reader) {
    return translatableWithContext("argument.pos3d.incomplete", reader);
  }

  @Override
  public CommandSyntaxException mixedPosition(StringReader reader) {
    return translatableWithContext("argument.pos.mixed", reader);
  }

  @Override
  public CommandSyntaxException unknownOption(StringReader reader,
                                              String usedLabel
  ) {
    return translatableWithContext("argument.entity.options.unknown",
        reader, usedLabel
    );
  }

  @Override
  public CommandSyntaxException optionAlreadySet(String word,
                                                 StringReader reader
  ) {
    return translatableWithContext(
        "argument.entity.options.inapplicable",
        reader,
        word
    );
  }

  @Override
  public CommandSyntaxException flagAlreadySet(String word,
                                               StringReader reader
  ) {
    return optionAlreadySet(word, reader);
  }

  @Override
  public CommandSyntaxException missingOption(ArgumentOption<?> option) {
    return create("Missing option '%s'", option.getLabels().iterator().next());
  }

  @Override
  public CommandSyntaxException rangeEmpty(StringReader reader) {
    return translatableWithContext("argument.range.empty", reader);
  }

  @Override
  public CommandSyntaxException rangeInverted(StringReader reader) {
    return translatableWithContext("argument.range.swapped", reader);
  }
}
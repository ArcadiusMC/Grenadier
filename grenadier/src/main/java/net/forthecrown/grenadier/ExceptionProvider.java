package net.forthecrown.grenadier;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.forthecrown.grenadier.types.options.Option;
import net.forthecrown.nbt.path.PathParseException;
import net.forthecrown.nbt.string.TagParseException;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

public interface ExceptionProvider extends CommandExceptionType {


  CommandSyntaxException create(String message, Object... args);

  CommandSyntaxException create(Component message);

  CommandSyntaxException createWithContext(String message,
                                           ImmutableStringReader reader,
                                           Object... args
  );

  CommandSyntaxException createWithContext(Component message,
                                           ImmutableStringReader reader
  );

  CommandSyntaxException overstacked(Material material);

  CommandSyntaxException unknownWorld(String name, StringReader reader);

  CommandSyntaxException unknownMapValue(String word, StringReader reader);

  CommandSyntaxException invalidKey(String name, StringReader reader);

  <E extends Enum<E>> CommandSyntaxException invalidEnum(Class<E> enumType,
                                                         String word,
                                                         StringReader reader
  );

  CommandSyntaxException unknownResource(NamespacedKey key,
                                         String registryName,
                                         StringReader reader
  );

  CommandSyntaxException pathParseException(PathParseException exc,
                                            StringReader reader
  );

  CommandSyntaxException tagParseException(TagParseException exc,
                                           StringReader reader
  );

  CommandSyntaxException unknownGamemode(String word, StringReader reader);

  CommandSyntaxException invalidTimeUnit(String word, StringReader reader);

  CommandSyntaxException unknownObjective(String word, StringReader reader);

  CommandSyntaxException unknownTeam(String word, StringReader reader);

  <T extends CommandSender> CommandSyntaxException sourceMustBe(Class<T> clazz);

  CommandSyntaxException selectorOnlyOnePlayer(StringReader reader);

  CommandSyntaxException selectorOnlyOneEntity(StringReader reader);

  CommandSyntaxException selectorOnlyPlayersAllowed(StringReader reader);

  CommandSyntaxException noPlayerFound();

  CommandSyntaxException noEntityFound();

  CommandSyntaxException unknownLootTable(NamespacedKey key, StringReader reader);

  CommandSyntaxException posNotComplete(StringReader reader);

  CommandSyntaxException mixedPosition(StringReader reader);

  CommandSyntaxException unknownOption(StringReader reader, String usedLabel);

  CommandSyntaxException optionAlreadySet(String word, StringReader reader);

  CommandSyntaxException flagAlreadySet(String word, StringReader reader);

  CommandSyntaxException missingOption(
      Option option,
      Collection<Option> exclusive,
      Collection<Option> requires
  );

  CommandSyntaxException rangeEmpty(StringReader reader);

  CommandSyntaxException rangeInverted(StringReader reader);

  CommandSyntaxException unknownSuffix(StringReader reader, String suffix);

  CommandSyntaxException exclusiveOption(String label, Collection<Option> excl);

  CommandSyntaxException missingRequired(String label, Collection<Option> req);
}
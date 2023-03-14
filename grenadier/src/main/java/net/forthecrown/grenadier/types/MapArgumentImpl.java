package net.forthecrown.grenadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import java.util.regex.Pattern;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;

class MapArgumentImpl<T> implements MapArgument<T> {

  public static final Pattern VALID_KEY_PATTERN = Pattern.compile("\\S+");

  private final Map<String, T> values;

  public MapArgumentImpl(Map<String, T> values) {
    this.values = values;
    values.keySet().forEach(MapArgumentImpl::validateKey);
  }

  static void validateKey(String name) {
    if (VALID_KEY_PATTERN.pattern().matches(name)) {
      return;
    }

    throw new IllegalArgumentException(
        String.format(
            "Invalid key '%s', must contain no whitespace characters",
            name
        )
    );
  }

  @Override
  public T parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();
    var word = Readers.readUntilWhitespace(reader);

    T value = values.get(word);

    if (value == null) {
      reader.setCursor(start);
      throw Grenadier.exceptions().unknownMapValue(word, reader);
    }

    return value;
  }

  @Override
  public Map<String, T> values() {
    return values;
  }
}
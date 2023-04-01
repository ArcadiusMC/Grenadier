package net.forthecrown.grenadier.annotations.tree;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Getter
@RequiredArgsConstructor
public class ArgumentValue<T> {
  final T value;

  @Setter
  List<Object> mappedValues;

  public Object findValue(Class<?> hint) {
    if (hint.isInstance(value)) {
      return value;
    }

    if (mappedValues == null || mappedValues.isEmpty()) {
      return null;
    }

    for (var o: mappedValues) {
      if (hint.isInstance(o)) {
        return o;
      }
    }

    return null;
  }
}
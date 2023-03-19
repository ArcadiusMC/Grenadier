package net.forthecrown.grenadier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import net.forthecrown.grenadier.types.ArgumentTypes;
import org.junit.jupiter.api.Test;

public class MapArgumentTest {

  @Test
  void testInstantiation() {
    Map<String, Integer> validMap = Map.of(
        "entry_1", 1224,
        "entry_2", 34234352,
        "entry_3", 121414
    );

    Map<String, Integer> invalidMap = Map.of(
        "has whitespace", 121414
    );

    assertDoesNotThrow(() -> {
      ArgumentTypes.map(validMap);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      ArgumentTypes.map(invalidMap);
    });
  }
}
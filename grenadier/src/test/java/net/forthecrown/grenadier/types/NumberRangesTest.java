package net.forthecrown.grenadier.types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.brigadier.PaperBrigadierProviderImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NumberRangesTest {

  @BeforeAll
  static void setup() {
    var instance = PaperBrigadierProviderImpl.INSTANCE;
  }

  @Test
  void parseDoubles() {

    assertThrows(CommandSyntaxException.class, () -> {
      StringReader reader = new StringReader("2..1");
      NumberRanges.parseDoubles(reader);
    });

    assertDoesNotThrow(() -> {
      StringReader reader = new StringReader("1.23..3.254325");
      NumberRanges.parseDoubles(reader);
    });
  }

  @Test
  void parseInts() {
  }
}
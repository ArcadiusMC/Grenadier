package net.forthecrown.grenadier;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.brigadier.StringReader;
import org.junit.jupiter.api.Test;

class GrenadierCommandNodeTest {

  @Test
  void parse() {
    GrenadierCommandNode node = new GrenadierCommand("label")
        .withAliases("label2", "label3")
        .build();

    StringReader correct1 = new StringReader("label asdas a");
    StringReader correct2 = new StringReader("label2");
    StringReader correct3 = new StringReader("label3 as asd aa ");

    StringReader wrong1 = new StringReader("labels as a ");
    StringReader wrong2 = new StringReader("labels23");
    StringReader wrong3 = new StringReader("label23 foo bar");

    assertTrue(node.parse(correct1) > 0);
    assertTrue(node.parse(correct2) > 0);
    assertTrue(node.parse(correct3) > 0);

    assertFalse(node.parse(wrong1) > 0);
    assertFalse(node.parse(wrong2) > 0);
    assertFalse(node.parse(wrong3) > 0);

  }
}
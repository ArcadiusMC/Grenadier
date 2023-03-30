package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext.DefaultExecutionRule;
import net.forthecrown.grenadier.annotations.tree.DebugVisitor;
import net.forthecrown.grenadier.annotations.tree.RootTree;
import org.junit.jupiter.api.Test;

public class ParserTest {

  public static final String TEST_STRING = LexerTest.TEST_STRING;

  @Test
  void test() {
    StringReader reader = new StringReader(TEST_STRING);
    ParseExceptions exceptions = ParseExceptions.factory(reader);

    Lexer lexer = new Lexer(reader, exceptions);

    Parser parser = new Parser(
        lexer,
        "defaultExecutionMethod",
        DefaultExecutionRule.IF_NO_CHILDREN
    );

    RootTree tree = parser.parse();

    DebugVisitor visitor = new DebugVisitor(new StringBuilder());
    tree.accept(visitor, null);

    System.out.print(visitor.getBuilder());
    System.out.print("\n");
  }
}
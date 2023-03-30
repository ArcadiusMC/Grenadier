package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LexerTest {
  public static final String TEST_STRING = """
      name = 'command_name'
      permission = @default_permission
      aliases = alias1 | alias2 | alias3
            
      // This is a comment
      literal('argument_name') {
        executes = withLiteralArg()
      }
      
      literal('no_kids') = noKids()
            
      /* This is also a comment */
      argument('entities', entity_selector(multiple = true, entities = true)) {
        suggests = ['1', '2', '3']
        
        requires = permission('ftc.commands.asd')
        executes = withSelectorArg()
      }
      
      argument('a_number', int(min=1, max=2)) {
        suggests = methodName()
        executes = methodName2()
      }
      
      argument('argName', @type_ref) {
        executes = method()
      }
      """;

  @Test
  void test() {
    StringReader reader = new StringReader(TEST_STRING);
    ParseExceptions exceptions = ParseExceptions.factory(reader);

    Lexer lexer = new Lexer(reader, exceptions);

    List<Token> tokens = new ArrayList<>();
    while (lexer.hasNext()) {
      tokens.add(lexer.next());
    }

    tokens.forEach(token -> {
      System.out.printf("| %-3s | %-20s | %-20s |\n",
          token.position(), token.type(), token.value()
      );
    });
  }

}
package net.forthecrown.grenadier.annotations;

import static net.forthecrown.grenadier.annotations.TokenType.ALIASES;
import static net.forthecrown.grenadier.annotations.TokenType.ARGUMENT;
import static net.forthecrown.grenadier.annotations.TokenType.ASSIGN;
import static net.forthecrown.grenadier.annotations.TokenType.BRACKET_CLOSE;
import static net.forthecrown.grenadier.annotations.TokenType.BRACKET_OPEN;
import static net.forthecrown.grenadier.annotations.TokenType.COMMA;
import static net.forthecrown.grenadier.annotations.TokenType.DESCRIPTION;
import static net.forthecrown.grenadier.annotations.TokenType.DOT;
import static net.forthecrown.grenadier.annotations.TokenType.EXECUTES;
import static net.forthecrown.grenadier.annotations.TokenType.IDENTIFIER;
import static net.forthecrown.grenadier.annotations.TokenType.LITERAL;
import static net.forthecrown.grenadier.annotations.TokenType.NAME;
import static net.forthecrown.grenadier.annotations.TokenType.PERMISSION;
import static net.forthecrown.grenadier.annotations.TokenType.QUOTED_STRING;
import static net.forthecrown.grenadier.annotations.TokenType.REQUIRES;
import static net.forthecrown.grenadier.annotations.TokenType.SCOPE_BEGIN;
import static net.forthecrown.grenadier.annotations.TokenType.SCOPE_END;
import static net.forthecrown.grenadier.annotations.TokenType.SQUARE_CLOSE;
import static net.forthecrown.grenadier.annotations.TokenType.SQUARE_OPEN;
import static net.forthecrown.grenadier.annotations.TokenType.SUGGESTS;
import static net.forthecrown.grenadier.annotations.TokenType.VARIABLE;
import static net.forthecrown.grenadier.annotations.TokenType.WALL;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext.DefaultExecutionRule;
import net.forthecrown.grenadier.annotations.tree.AbstractCmdTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.TypeInfoTree;
import net.forthecrown.grenadier.annotations.tree.ArgumentTypeRef.VariableTypeRef;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef;
import net.forthecrown.grenadier.annotations.tree.ClassComponentRef.Kind;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.RefExecution;
import net.forthecrown.grenadier.annotations.tree.ExecutesTree.VariableExecutes;
import net.forthecrown.grenadier.annotations.tree.LiteralTree;
import net.forthecrown.grenadier.annotations.tree.Name;
import net.forthecrown.grenadier.annotations.tree.Name.DirectName;
import net.forthecrown.grenadier.annotations.tree.Name.VariableName;
import net.forthecrown.grenadier.annotations.tree.RequiresTree;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.PermissionRequires;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.RequiresRef;
import net.forthecrown.grenadier.annotations.tree.RequiresTree.VariableRequires;
import net.forthecrown.grenadier.annotations.tree.RootTree;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.ComponentRefSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.StringListSuggestions;
import net.forthecrown.grenadier.annotations.tree.SuggestsTree.VariableSuggestions;

@Getter
class Parser {

  static final int CONTEXT_OFFSET = 25;

  static final TokenType[] WITH_ID_NAME_TOKENS = {
      QUOTED_STRING, IDENTIFIER, VARIABLE
  };

  static final TokenType[] NO_ID_NAME_TOKENS = { QUOTED_STRING, VARIABLE };

  private final Lexer lexer;
  private final String defaultExecutes;
  private final DefaultExecutionRule rule;

  private final ParseExceptions factory;

  public Parser(
      Lexer lexer,
      String defaultExecutes,
      DefaultExecutionRule defaultExecutionRule
  ) {
    this.lexer = lexer;
    this.factory = lexer.getFactory();

    this.defaultExecutes = defaultExecutes;
    this.rule = defaultExecutionRule;
  }

  public RootTree parse() {
    RootTree tree = new RootTree();

    lexer.expect(NAME);
    lexer.expect(ASSIGN);

    var name = parseName(false);
    tree.setName(name);

    while (lexer.hasNext()) {
      if (baseBodyParse(tree)) {
        continue;
      }

      var next = lexer.expect(
          PERMISSION, ALIASES, DESCRIPTION,
          ARGUMENT, EXECUTES, REQUIRES,
          LITERAL
      );

      lexer.expect(ASSIGN);

      if (next.is(PERMISSION)) {
        ensureCanSet(tree.getPermission(), PERMISSION, tree);

        Name permission = parseName(false);
        tree.setPermission(permission);

        continue;
      }

      if (next.is(DESCRIPTION)) {
        ensureCanSet(tree.getDescription(), DESCRIPTION, tree);

        var desc = lexer.expect(QUOTED_STRING).value();
        tree.setDescription(desc);

        continue;
      }

      if (next.is(ALIASES)) {
        ensureCanSet(tree.getAliases(), ALIASES, tree);

        var aliases = parseAliases();
        tree.setAliases(aliases);
      }
    }

    if (shouldSetDefault(tree)) {
      tree.setExecutes(executesFromString(defaultExecutes));
    }

    return tree;
  }

  private boolean shouldSetDefault(RootTree tree) {
    if (tree.getExecutes() != null || Strings.isNullOrEmpty(defaultExecutes)) {
      return false;
    }

    if (rule == DefaultExecutionRule.IF_MISSING) {
      return true;
    }

    return tree.getChildren().isEmpty();
  }

  private ExecutesTree executesFromString(String s) {
    if (s.startsWith("@")) {
      String variable = s.substring(1);
      return new VariableExecutes(variable);
    }

    return new RefExecution(new ClassComponentRef(s, Kind.METHOD, null));
  }

  List<Name> parseAliases() {
    List<Name> aliases = new ArrayList<>();

    while (true) {
      aliases.add(parseName(true));

      if (!lexer.peek().is(WALL)) {
        break;
      } else {
        lexer.next();
      }
    }

    return aliases;
  }

  public ArgumentTree parseArgument() {
    lexer.expect(ARGUMENT);
    lexer.expect(BRACKET_OPEN);

    var name = parseName(false);

    lexer.expect(COMMA);

    ArgumentTypeRef typeInfo = parseTypeInfo();

    lexer.expect(BRACKET_CLOSE);

    ArgumentTree tree = new ArgumentTree();
    tree.setName(name);
    tree.setTypeInfo(typeInfo);

    parseNodeBody(tree, () -> {
      if (baseBodyParse(tree)) {
        return;
      }

      if (lexer.peek().is(SUGGESTS)) {
        ensureCanSet(tree.getSuggests(), "suggests", tree);

        lexer.expect(SUGGESTS);
        lexer.expect(ASSIGN);

        var suggests = parseSuggestsValue();
        tree.setSuggests(suggests);
        return;
      }

      lexer.expect(EXECUTES, SUGGESTS, REQUIRES, ARGUMENT, LITERAL);
    });

    return tree;
  }

  void parseNodeBody(AbstractCmdTree tree, Runnable scopeParseLoop) {
    if (lexer.peek().is(ASSIGN)) {
      lexer.expect(ASSIGN);
      parseExecutes(tree);
    } else {
      parseScope(scopeParseLoop);
    }
  }

  ArgumentTypeRef parseTypeInfo() {
    if (lexer.peek().is(VARIABLE)) {
      return new VariableTypeRef(lexer.next().value());
    }

    Token typeToken = lexer.expect(IDENTIFIER);

    if (!lexer.peek().is(BRACKET_OPEN)) {
      return new TypeInfoTree(typeToken.value(), Collections.emptyMap());
    }

    Map<String, Token> options = new HashMap<>();
    parseParentheses(() -> {
      var label = lexer.expect(IDENTIFIER);
      lexer.expect(ASSIGN);

      var value = lexer.next();
      options.put(label.value(), value);
    });

    return new TypeInfoTree(typeToken.value(), options);
  }

  public LiteralTree parseLiteral() {
    lexer.expect(LITERAL);
    lexer.expect(BRACKET_OPEN);

    var name = parseName(false);

    lexer.expect(BRACKET_CLOSE);

    LiteralTree tree = new LiteralTree();
    tree.setName(name);

    parseNodeBody(tree, () -> {
      if (baseBodyParse(tree)) {
        return;
      }

      lexer.expect(EXECUTES, REQUIRES, ARGUMENT, LITERAL);
    });

    return tree;
  }

  boolean baseBodyParse(AbstractCmdTree tree) {
    var peek = lexer.peek();

    if (peek.is(EXECUTES)) {
      lexer.expect(EXECUTES);
      lexer.expect(ASSIGN);

      parseExecutes(tree);
      return true;
    }

    if (peek.is(REQUIRES)) {
      lexer.expect(REQUIRES);
      lexer.expect(ASSIGN);

      parseRequires(tree);
      return true;
    }

    if (peek.is(LITERAL)) {
      var literal = parseLiteral();
      tree.getChildren().add(literal);
      return true;
    }

    if (peek.is(ARGUMENT)) {
      var argument = parseArgument();
      tree.getChildren().add(argument);
      return true;
    }

    return false;
  }

  void parseExecutes(AbstractCmdTree tree) {
    ensureCanSet(tree.getExecutes(), "executes", tree);

    var exec = parseExecutes();
    tree.setExecutes(exec);
  }

  void parseRequires(AbstractCmdTree tree) {
    ensureCanSet(tree.getRequires(), "requires", tree);

    var requirement = parseRequires();
    tree.setRequires(requirement);
  }

  void ensureCanSet(Object value, TokenType field, AbstractCmdTree tree) {
    ensureCanSet(value, field.toString(), tree);
  }

  void ensureCanSet(Object value, String field, AbstractCmdTree tree) {
    if (value == null) {
      return;
    }

    throw factory.create(
        lexer.getLastStart(),
        "Tried to redefine '%s' of node '%s'",
        field, tree.getName()
    );
  }

  public SuggestsTree parseSuggestsValue() {
    if (lexer.peek().is(VARIABLE)) {
      return new VariableSuggestions(lexer.next().value());
    }

    if (!lexer.peek().is(SQUARE_OPEN)) {
      var ref = parseComponentRef();
      return new ComponentRefSuggestions(ref);
    }

    List<String> strings = new ArrayList<>();

    parseSquareBrackets(() -> {
      String suggestion = lexer.expect(QUOTED_STRING).value();
      strings.add(suggestion);
    });

    return new StringListSuggestions(strings.toArray(String[]::new));
  }

  public RequiresTree parseRequires() {
    var peek = lexer.peek();

    if (peek.is(VARIABLE)) {
      return new VariableRequires(lexer.next().value());
    }

    if (!peek.is(PERMISSION)) {
      ClassComponentRef ref = parseComponentRef();
      return new RequiresRef(ref);
    }

    // Skip 'permission' token
    lexer.next();

    lexer.expect(BRACKET_OPEN);
    Name name = parseName(false);
    lexer.expect(BRACKET_CLOSE);

    return new PermissionRequires(name);
  }

  private ExecutesTree parseExecutes() {
    if (lexer.peek().is(VARIABLE)) {
      return new VariableExecutes(lexer.next().value());
    }

    ClassComponentRef ref = parseComponentRef();
    return new RefExecution(ref);
  }

  private Name parseName(boolean allowId) {
    var token = lexer.expect(
        allowId
            ? WITH_ID_NAME_TOKENS
            : NO_ID_NAME_TOKENS
    );

    if (token.is(VARIABLE)) {
      return new VariableName(token.value());
    }

    return new DirectName(token.value());
  }

  private ClassComponentRef parseComponentRef() {
    Token labelToken = lexer.expect(IDENTIFIER);
    String label = labelToken.value();

    Kind kind;

    if (lexer.peek().is(BRACKET_OPEN)) {
      kind = Kind.METHOD;

      lexer.expect(BRACKET_OPEN);
      lexer.expect(BRACKET_CLOSE);
    } else {
      kind = Kind.FIELD;
    }

    ClassComponentRef next;

    if (lexer.peek().is(DOT)) {
      lexer.expect(DOT);
      next = parseComponentRef();
    } else {
      next = null;
    }

    return new ClassComponentRef(label, kind, next);
  }

  /* ---------------------- DELIMITER-BASED PARSING ----------------------- */

  void parseSquareBrackets(Runnable body) {
    parseDelimited(SQUARE_OPEN, SQUARE_CLOSE, true, body);
  }

  void parseScope(Runnable scopeParseLoop) {
    parseDelimited(SCOPE_BEGIN, SCOPE_END, false, scopeParseLoop);
  }

  void parseParentheses(Runnable loopBody) {
    parseDelimited(BRACKET_OPEN, BRACKET_CLOSE, true, loopBody);
  }

  void parseDelimited(TokenType open,
                      TokenType close,
                      boolean requireComma,
                      Runnable loopBody
  ) {
    lexer.expect(open);

    while (!lexer.peek().is(close)) {
      loopBody.run();

      if (requireComma) {
        if (lexer.peek().is(COMMA)) {
          lexer.next();
          continue;
        } else if (lexer.peek().is(close)) {
          break;
        }

        lexer.expect(COMMA, close);
      }
    }

    lexer.expect(close);
  }
}
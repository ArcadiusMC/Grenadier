package net.forthecrown.grenadier.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum TokenType {
  // Reserved keywords
  NAME            ("name"),
  PERMISSION      ("permission"),
  DESCRIPTION     ("description"),
  ALIASES         ("aliases"),
  EXECUTES        ("executes"),
  SUGGESTS        ("suggests"),
  REQUIRES        ("requires"),
  LITERAL         ("literal"),
  ARGUMENT        ("argument"),
  TYPE_MAP        ("map_result"),
  SYNTAX_LABEL    ("label"),
  TRANSLATABLE    ("translatable"),
  PLAIN_TRANS     ("translate_plain"),

  TRUE            ("true"),
  FALSE           ("false"),

  // Single character tokens
  ASSIGN          ('='),
  COMMA           (','),
  DOT             ('.'),
  WALL            ('|'),
  HASHTAG         ('#'),

  // Brackets
  SCOPE_BEGIN     ('{'),
  SCOPE_END       ('}'),
  BRACKET_OPEN    ('('),
  BRACKET_CLOSE   (')'),
  SQUARE_OPEN     ('['), // Currently unused
  SQUARE_CLOSE    (']'), // Currently unused

  // Values
  QUOTED_STRING,
  IDENTIFIER,
  VARIABLE,

  // What a mess of regex lmao, this is bad regex and should be replaced
  // at some point. It can match empty strings, but it works for the same kind
  // of double input as in java source files... hopefully
  NUMBER (Pattern.compile("([+-]?([0-9]+)?(\\.([0-9]+([eE][+-]?[0-9]+)?)?)?){1}")),

  EOF;

  static final Map<Character, TokenType> charLookup;
  static final Map<String, TokenType> stringLookup;
  static final List<TokenType> patternLookup;

  static {
    Map<Character, TokenType> charMap = new HashMap<>();
    Map<String, TokenType> stringMap = new HashMap<>();
    List<TokenType> patternMap = new ArrayList<>();

    for (var t: values()) {
      if (t.getCharacterValue() != null) {
        charMap.put(t.getCharacterValue(), t);
        continue;
      }

      if (t.getStringValue() != null) {
        stringMap.put(t.getStringValue(), t);
        continue;
      }

      if (t.getPattern() != null) {
        patternMap.add(t);
      }
    }

    charLookup = Collections.unmodifiableMap(charMap);
    stringLookup = Collections.unmodifiableMap(stringMap);
    patternLookup = Collections.unmodifiableList(patternMap);
  }

  private final Character characterValue;
  private final String stringValue;
  private final Pattern pattern;

  TokenType() {
    this.characterValue = null;
    this.stringValue = null;
    this.pattern = null;
  }

  TokenType(Character characterValue) {
    this.characterValue = characterValue;
    this.stringValue = null;
    this.pattern = null;
  }

  TokenType(String stringValue) {
    this.characterValue = null;
    this.pattern = null;
    this.stringValue = stringValue;
  }

  TokenType(Pattern pattern) {
    String patternString = pattern.pattern();

    // Prevent patterns being able to match input that isn't
    // being read currently
    if (patternString.startsWith("^")) {
      this.pattern = pattern;
    } else {
      this.pattern = Pattern.compile("^" + patternString);
    }

    this.characterValue = null;
    this.stringValue = null;
  }

  public static @Nullable TokenType fromCharacter(char c) {
    return charLookup.get(c);
  }

  public static @Nullable TokenType keyword(String word) {
    return stringLookup.get(word);
  }

  public Token token(int position) {
    return token(position, "");
  }

  public Token token(int position, String input) {
    return new TokenImpl(input, this, position);
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
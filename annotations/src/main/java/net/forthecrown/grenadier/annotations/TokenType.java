package net.forthecrown.grenadier.annotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
  TYPE_MAP        ("map_type"),

  TRUE            ("true"),  // Currently unused
  FALSE           ("false"), // Currently unused

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

  EOF;

  static final Map<Character, TokenType> charLookup;
  static final Map<String, TokenType> stringLookup;

  static {
    Map<Character, TokenType> charMap = new HashMap<>();
    Map<String, TokenType> stringMap = new HashMap<>();

    for (var t: values()) {
      if (t.getCharacterValue() != null) {
        charMap.put(t.getCharacterValue(), t);
        continue;
      }

      if (t.getStringValue() != null) {
        stringMap.put(t.getStringValue(), t);
      }
    }

    charLookup = Collections.unmodifiableMap(charMap);
    stringLookup = Collections.unmodifiableMap(stringMap);
  }

  private final Character characterValue;
  private final String stringValue;

  TokenType() {
    this.characterValue = null;
    this.stringValue = null;
  }

  TokenType(Character characterValue) {
    this.characterValue = characterValue;
    this.stringValue = null;
  }

  TokenType(String stringValue) {
    this.characterValue = null;
    this.stringValue = stringValue;
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
    return new Token(input, this, position);
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
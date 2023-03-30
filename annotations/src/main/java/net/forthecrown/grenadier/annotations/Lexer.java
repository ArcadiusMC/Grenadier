package net.forthecrown.grenadier.annotations;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Iterator;
import java.util.Locale;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.Readers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;

@RequiredArgsConstructor
class Lexer implements Iterator<Token> {

  @Getter
  private final StringReader reader;

  @Getter
  private final ParseExceptions factory;

  private Token peeked;

  @Getter
  private int lastStart;

  @Override
  public boolean hasNext() {
    skipWhitespace();
    return reader.canRead();
  }

  public Token peek() {
    if (peeked != null) {
      return peeked;
    }

    return peeked = parseToken();
  }

  public Token expect(TokenType... types) throws CommandParseException {
    var next = next();
    next.expect(factory, types);
    return next;
  }

  @Override
  public Token next() {
    if (peeked != null) {
      var p = peeked;
      peeked = null;
      return p;
    }

    return parseToken();
  }

  private Token parseToken() throws CommandParseException {
    try {
      var token = _parseToken();
      lastStart = token.position();
      return token;
    } catch (CommandSyntaxException exc) {
      Component msg = Grenadier.fromMessage(exc.getRawMessage());
      Component rendered = GlobalTranslator.render(msg, Locale.ENGLISH);
      String stringMessage = PlainTextComponentSerializer.plainText()
          .serialize(rendered);

      throw factory.create(reader.getCursor(), stringMessage);
    }
  }

  private Token _parseToken() throws CommandSyntaxException {
    if (!hasNext()) {
      return TokenType.EOF.token(reader.getCursor());
    }

    char c = reader.peek();
    int p = reader.getCursor();

    // Variable name token
    if (c == '@') {
      reader.skip();
      String word = reader.readUnquotedString();

      if (word.isEmpty()) {
        throw factory.create(p,
            "Variable prefix '@' must be followed by an identifier"
        );
      }

      return TokenType.VARIABLE.token(p, word);
    }

    TokenType fromCharType = TokenType.fromCharacter(c);

    if (fromCharType != null) {
      reader.skip();
      return fromCharType.token(p);
    }

    if (StringReader.isQuotedStringStart(c)) {
      String quoted = reader.readQuotedString();
      return TokenType.QUOTED_STRING.token(p, quoted);
    }

    String word = reader.readUnquotedString();

    if (word.isEmpty()) {
      throw factory.create(p, "Unknown token '%s'", c);
    }

    var keyword = TokenType.keyword(word);

    if (keyword == null) {
      return TokenType.IDENTIFIER.token(p, word);
    }

    return keyword.token(p);
  }

  private void skipWhitespace() {
    while (reader.canRead()) {
      char c = reader.peek();

      if (Character.isWhitespace(c)) {
        reader.skip();
        continue;
      }

      if (Readers.startsWith(reader, "//")) {
        skipLineComment();
        continue;
      }

      if (Readers.startsWith(reader, "/*")) {
        skipStarComment();
        continue;
      }

      break;
    }
  }

  private void skipStarComment() {
    while (reader.canRead()) {
      if (Readers.startsWith(reader, "*/")) {
        reader.skip();
        reader.skip();
        break;
      }

      reader.skip();
    }
  }

  private void skipLineComment() {
    while (reader.canRead() && reader.peek() != '\n') {
      reader.skip();
    }
  }
}
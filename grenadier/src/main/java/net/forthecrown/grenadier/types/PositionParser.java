package net.forthecrown.grenadier.types;

import static net.forthecrown.grenadier.types.PositionArgumentImpl.FLAG_2D;
import static net.forthecrown.grenadier.types.PositionArgumentImpl.FLAG_BLOCK;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.grenadier.types.ParsedPosition.Coordinate;
import net.forthecrown.grenadier.types.ParsedPosition.Type;

@Getter
class PositionParser {

  private final StringReader reader;
  private final PositionArgumentImpl argument;
  private final Type type;

  public PositionParser(StringReader reader, PositionArgumentImpl argument) {
    this.reader = reader;
    this.argument = argument;

    if (reader.canRead() && reader.peek() == Type.LOCAL.character()) {
      type = Type.LOCAL;
    } else {
      type = Type.WORLD;
    }
  }

  public ParsedPosition parse() throws CommandSyntaxException {
    if (type == Type.LOCAL) {
      var x = parseCoordinate();
      var y = parseCoordinate();
      var z = parseCoordinate();

      return new LocalParsedPosition(x.value(), y.value(), z.value());
    }

    Coordinate x = parseCoordinate();
    Coordinate y;
    Coordinate z;

    if (argument.has(FLAG_2D)) {
      y = null;
    } else {
      y = parseCoordinate();
    }

    z = parseCoordinate();

    return new ParsedPositionImpl(x, y, z);
  }

  public Coordinate parseCoordinate() throws CommandSyntaxException {
    reader.skipWhitespace();

    if (!reader.canRead()) {
      throw Grenadier.exceptions().posNotComplete(reader);
    }

    boolean relative;
    double value;

    if (type == Type.LOCAL) {
      relative = true;
      reader.expect(type.character());
    } else {
      char peeked = reader.peek();

      if (peeked == Type.LOCAL.character()) {
        throw Grenadier.exceptions().mixedPosition(reader);
      }

      if (peeked == type.character()) {
        relative = true;
        reader.skip();
      } else {
        relative = false;
      }
    }

    if (!reader.canRead() || Character.isWhitespace(reader.peek())) {
      if (!relative) {
        throw Grenadier.exceptions().posNotComplete(reader);
      }

      value = 0.0D;
    } else {
      boolean readInteger = argument.has(FLAG_BLOCK);

      value = readInteger
          ? reader.readInt()
          : reader.readDouble();
    }

    return new Coordinate(value, relative);
  }
}
package net.forthecrown.grenadier.types;

import java.util.Objects;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.Location;

public interface ParsedPosition {

  int AXIS_X = 0;
  int AXIS_Y = 1;
  int AXIS_Z = 2;

  Coordinate EMPTY_COORDINATE = new Coordinate(0.0D, true);

  ParsedPosition IDENTITY = new IdentityPosition();

  default Location apply(CommandSource source) {
    return apply(source.getLocation());
  }

  Location apply(Location base);

  Coordinate[] getCoordinates();

  Type getType();

  default boolean isTwoDimensional() {
    return getYCoordinate() == null;
  }

  default boolean isIdentity() {
    Coordinate x = getXCoordinate();
    Coordinate y = getYCoordinate();
    Coordinate z = getZCoordinate();

    return Objects.equals(x, EMPTY_COORDINATE)
        && (y == null || Objects.equals(y, EMPTY_COORDINATE))
        && Objects.equals(z, EMPTY_COORDINATE);
  }

  default Coordinate getXCoordinate() {
    return getCoordinates()[AXIS_X];
  }

  default Coordinate getYCoordinate() {
    return getCoordinates()[AXIS_Y];
  }

  default Coordinate getZCoordinate() {
    return getCoordinates()[AXIS_Z];
  }

  /**
   * Single parsed coordinate
   * @param value Coordinate value
   * @param relative Whether the coordinate is relative or absolute
   * @see #apply(double)
   */
  record Coordinate(double value, boolean relative) {

    /**
     * Applies this coordinate to the specified {@code base}
     * @param base Base coordinate
     * @return {@code base + value} if relative, {@code value} otherwise
     */
    public double apply(double base) {
      return relative() ? base + value() : value();
    }
  }

  /**
   * Position local coordinate type
   */
  enum Type {
    /** Position is local to a command source's world coordinates */
    WORLD,

    /** Position is local to the direction a command source is facing */
    LOCAL
  }
}
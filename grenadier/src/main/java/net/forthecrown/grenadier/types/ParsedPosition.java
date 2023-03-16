package net.forthecrown.grenadier.types;

import java.util.Objects;
import net.forthecrown.grenadier.CommandSource;
import org.bukkit.Location;

public interface ParsedPosition {

  /** Index used to access the X coordinate in {@link #getCoordinates()} */
  int AXIS_X = 0;

  /** Index used to access the Y coordinate in {@link #getCoordinates()} */
  int AXIS_Y = 1;

  /** Index used to access the Z coordinate in {@link #getCoordinates()} */
  int AXIS_Z = 2;

  /**
   * Empty coordinate which always returns the base coordinate in
   * {@link Coordinate#apply(double)}
   */
  Coordinate EMPTY_COORDINATE = new Coordinate(0.0D, true);

  /**
   * Empty position that always returns the input in {@link #apply(Location)}
   */
  ParsedPosition IDENTITY = new IdentityPosition();

  /**
   * Applies this position's data to the anchored location of the specified
   * {@code source}
   * <p>
   * Uses {@link CommandSource#getAnchoredLocation()} for the location
   *
   * @param source Source to get the base location from
   * @return Applied location
   *
   * @see #apply(Location)
   */
  default Location apply(CommandSource source) {
    return apply(source.getAnchoredLocation());
  }

  /**
   * Applies this parsed position's data onto the specified {@code base}
   * <p>
   * The base location will have its X, Y and Z axes modified according to the
   * implementation of this interface and the parsed data. For most cases, each
   * coordinate in the {@link #getCoordinates()} array will be applied to the
   * base location. In the case of local coordinates however
   * (example: {@code ^2 ^1 ^}), the location will be moved forwards, up, and
   * to the left relative to the direction its facing
   * <p>
   * Be aware, the {@code base} will be modified and returned
   *
   * @param base base location to modify
   * @return Modified location
   */
  Location apply(Location base);

  /**
   * Gets all the coordinates of this parsed position.
   * <p>
   * The returned array will always have a length of 3. The 2nd element, or aka
   * element at index 1, may be null, if the parsed position was 2D instead of
   * 3D
   * <p>
   * If {@link #getType()} is {@link Type#LOCAL} then all returned coordinates
   * will be relative
   *
   * @return This position's coordinates
   */
  Coordinate[] getCoordinates();

  /**
   * Gets the parsed location's type. Determines how {@link #apply(Location)}
   * will handle the input.
   *
   * @return Position's type
   */
  Type getType();

  /**
   * Tests if this position is two-dimensional
   * @return {@code true}, if this position is 2D, aka if the
   *         {@link #getYCoordinate()} is {@code null}, {@code false} otherwise
   */
  default boolean isTwoDimensional() {
    return getYCoordinate() == null;
  }

  /**
   * Tests if this position will return the input in {@link #apply(Location)}
   * @return {@code true} if this position is empty, {@code false} otherwise
   */
  default boolean isIdentity() {
    Coordinate x = getXCoordinate();
    Coordinate y = getYCoordinate();
    Coordinate z = getZCoordinate();

    return Objects.equals(x, EMPTY_COORDINATE)
        && (y == null || Objects.equals(y, EMPTY_COORDINATE))
        && Objects.equals(z, EMPTY_COORDINATE);
  }

  /**
   * Gets the X coordinate
   * @return X Coordinate value
   */
  default Coordinate getXCoordinate() {
    return getCoordinates()[AXIS_X];
  }

  /**
   * Gets the Y coordinate
   * @return Y coordinate, or {@code null}, if this position is 2D
   */
  default Coordinate getYCoordinate() {
    return getCoordinates()[AXIS_Y];
  }

  /**
   * Gets the Z coordinate
   * @return Z coordinate
   */
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
    WORLD ('~'),

    /** Position is local to the direction a command source is facing */
    LOCAL ('^');

    private final char character;

    Type(char character) {
      this.character = character;
    }

    public char character() {
      return character;
    }
  }
}
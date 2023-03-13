package net.forthecrown.grenadier.types.position;

import net.forthecrown.grenadier.CommandSource;
import org.bukkit.Location;

public interface ParsedPosition {
  int AXIS_X = 0;
  int AXIS_Y = 1;
  int AXIS_Z = 2;

  Coordinate EMPTY_COORDINATE = new Coordinate(0.0D, true);

  ParsedPosition SELF = new SelfPosition();

  default Location apply(CommandSource source) {
    return apply(source.getLocation());
  }

  Location apply(Location base);

  Coordinate[] getCoordinates();

  default boolean isTwoDimensional() {
    return getYCoordinate() == null;
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

  record Coordinate(double value, boolean relative) {

    public double apply(double base) {
      return relative() ? base + value() : value();
    }
  }
}
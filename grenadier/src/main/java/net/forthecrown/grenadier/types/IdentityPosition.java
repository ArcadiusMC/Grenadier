package net.forthecrown.grenadier.types;

import org.bukkit.Location;

class IdentityPosition implements ParsedPosition {
  private static final Coordinate[] COORDINATES = {
      EMPTY_COORDINATE,
      EMPTY_COORDINATE,
      EMPTY_COORDINATE
  };

  @Override
  public Location apply(Location base) {
    return base;
  }

  @Override
  public Coordinate[] getCoordinates() {
    return COORDINATES.clone();
  }

  @Override
  public Type getType() {
    return Type.WORLD;
  }

  @Override
  public Coordinate getXCoordinate() {
    return EMPTY_COORDINATE;
  }

  @Override
  public Coordinate getYCoordinate() {
    return EMPTY_COORDINATE;
  }

  @Override
  public Coordinate getZCoordinate() {
    return EMPTY_COORDINATE;
  }
}
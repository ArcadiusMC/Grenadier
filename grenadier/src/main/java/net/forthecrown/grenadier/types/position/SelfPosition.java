package net.forthecrown.grenadier.types.position;

import org.bukkit.Location;

class SelfPosition implements ParsedPosition {
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
}
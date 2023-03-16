package net.forthecrown.grenadier.types;

import org.bukkit.Location;

class ParsedPositionImpl implements ParsedPosition {

  private final Coordinate[] coordinates;

  public ParsedPositionImpl(Coordinate... coordinates) {
    this.coordinates = coordinates;
  }

  @Override
  public Location apply(Location base) {
    var cX = getXCoordinate();
    var cY = getYCoordinate();
    var cZ = getZCoordinate();

    base.setX(cX.apply(base.getX()));
    base.setZ(cZ.apply(base.getZ()));

    if (cY != null) {
      base.setY(cY.apply(base.getY()));
    }

    return base;
  }

  @Override
  public Coordinate[] getCoordinates() {
    return coordinates.clone();
  }

  @Override
  public Type getType() {
    return Type.WORLD;
  }
}
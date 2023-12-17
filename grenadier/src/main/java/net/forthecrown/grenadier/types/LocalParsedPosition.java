package net.forthecrown.grenadier.types;

import org.bukkit.Location;
import org.bukkit.util.Vector;

class LocalParsedPosition implements ParsedPosition {

  private final Coordinate[] coordinates;

  private final double left;
  private final double up;
  private final double forwards;

  public LocalParsedPosition(double left, double up, double forwards) {
    coordinates = new Coordinate[3];
    coordinates[AXIS_X] = new Coordinate(left, true);
    coordinates[AXIS_Y] = new Coordinate(up, true);
    coordinates[AXIS_Z] = new Coordinate(forwards, true);

    this.left = left;
    this.up = up;
    this.forwards = forwards;
  }

  @Override
  public Location apply(Location base) {
    // Shamelessly stolen from:
    // https://www.spigotmc.org/threads/local-coordinates.529011/#post-4280379

    // Firstly a vector facing YAW = 0, on the XZ plane as start base
    Vector axisBase = new Vector(0, 0, 1);

    // This one pointing YAW + 90° should be the relative "left" of the field of
    // view, isn't it (since ROLL always is 0°)?
    Vector axisLeft = axisBase.clone().rotateAroundY(Math.toRadians(-base.getYaw() + 90.0f));

    // Left axis should be the rotation axis for going up, too, since it's perpendicular...
    Vector axisUp = base.getDirection().rotateAroundNonUnitAxis(axisLeft, Math.toRadians(-90f));

    // Based on these directions, we got all we need
    Vector sway = axisLeft.clone().normalize().multiply(left);
    Vector heave = axisUp.clone().normalize().multiply(up);
    Vector surge = base.getDirection().multiply(forwards);

    // Add up the global reference based result
    return base.add(sway).add(heave).add(surge);
  }

  @Override
  public Coordinate[] getCoordinates() {
    return coordinates.clone();
  }

  @Override
  public Coordinate getXCoordinate() {
    return coordinates[AXIS_X];
  }

  @Override
  public Coordinate getYCoordinate() {
    return coordinates[AXIS_Y];
  }

  @Override
  public Coordinate getZCoordinate() {
    return coordinates[AXIS_Z];
  }

  @Override
  public Type getType() {
    return Type.LOCAL;
  }
}
package net.forthecrown.grenadier.types;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftVector;

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
    var pos = CraftVector.toNMS(base.toVector());
    var rot = new Vec2(base.getYaw(), base.getPitch());

    var newPos = applyLocal(pos, rot);

    base.setX(newPos.x);
    base.setY(newPos.y);
    base.setZ(newPos.z);

    return base;
  }

  private Vec3 applyLocal(Vec3 pos, Vec2 rot) {
    // I won't even lie, I copy-pasted all of this code from
    // LocalCoordinates, aka from NMS code, in my defence,
    // I don't know trigonometry, or whatever type of math
    // this is, so uhhh... I had to
    //    - Jules <3

    var x = left;
    var y = up;
    var z = forwards;

    float f = Mth.cos((rot.y + 90.0F) * ((float)Math.PI / 180F));
    float g = Mth.sin((rot.y + 90.0F) * ((float)Math.PI / 180F));

    float h = Mth.cos(-rot.x * ((float)Math.PI / 180F));
    float i = Mth.sin(-rot.x * ((float)Math.PI / 180F));

    float j = Mth.cos((-rot.x + 90.0F) * ((float)Math.PI / 180F));
    float k = Mth.sin((-rot.x + 90.0F) * ((float)Math.PI / 180F));

    Vec3 vec32 = new Vec3(f * h, i, g * h);
    Vec3 vec33 = new Vec3(f * j, k, g * j);
    Vec3 vec34 = vec32.cross(vec33).scale(-1.0D);

    double d = vec32.x * x + vec33.x * y + vec34.x * z;
    double e = vec32.y * x + vec33.y * y + vec34.y * z;
    double l = vec32.z * x + vec33.z * y + vec34.z * z;

    return new Vec3(
        pos.x + d,
        pos.y + e,
        pos.z + l
    );
  }

  @Override
  public Coordinate[] getCoordinates() {
    return coordinates.clone();
  }

  @Override
  public Type getType() {
    return Type.LOCAL;
  }
}
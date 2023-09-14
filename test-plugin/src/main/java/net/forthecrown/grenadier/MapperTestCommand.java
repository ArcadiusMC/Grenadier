package net.forthecrown.grenadier;

import java.util.List;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandFile;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

@CommandFile("mapper_test.gcn")
public class MapperTestCommand {

  void runTest(
      CommandSource source,
      @Argument("first") Location location,
      @Argument("second") List<Entity> entities
  ) {
    source.sendMessage(location.toString());
    source.sendMessage(entities.toString());
  }
}

package net.forthecrown.grenadier;

import java.util.Map;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import org.bukkit.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandData("file = resource.gcn")
public class ResourcedCommandTest {

  static final String POS_ARG = "position";
  static final Logger LOGGER = LoggerFactory.getLogger("Resource tes");

  @VariableInitializer
  void initVars(Map<String, Object> maps) {
    maps.put("pos_arg", POS_ARG);
  }

  public void run1() {
    LOGGER.debug("run1");
  }

  public void run2() {
    LOGGER.debug("run2");
  }

  public void runPos(@Argument(POS_ARG) Location l) {
    LOGGER.debug("location={}", l);
  }
}
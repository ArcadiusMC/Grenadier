package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.grenadier.annotations.CommandData;

@CommandData("file = paste-dest.gcn")
public class PasteAnnotationTest {

  void an_argument(CommandContext<CommandSource> context) {
    System.out.println("an_argument");
  }

  void defaultExec(CommandContext<CommandSource> context) {
    System.out.println("defaultExec");
  }
}
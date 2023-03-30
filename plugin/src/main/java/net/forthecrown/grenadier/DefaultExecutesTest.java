package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.grenadier.annotations.CommandData;

@CommandData("name = 'def_exec_test'")
public class DefaultExecutesTest {
  public void defRun(CommandContext<CommandSource> context) {
    context.getSource().sendMessage("Hi, how are ya");
  }
}
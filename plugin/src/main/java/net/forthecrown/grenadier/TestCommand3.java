package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;

@CommandData("""
name = 'visit'
aliases = v | vr | visitregion

argument('word', word) {
  executes = run()
}
""")
public class TestCommand3 {

  public void run(CommandContext<CommandSource> context,
                  @Argument("word") String word
  ) {
    context.getSource().sendMessage(word);
  }
}
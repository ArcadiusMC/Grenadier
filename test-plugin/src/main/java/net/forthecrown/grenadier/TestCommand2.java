package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import java.util.Map;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;

@CommandData("""
name = 'test_command2'
aliases = test_alias_10
        | test_alias_20
        
argument(@argument_name, greedy_string) {
  executes = run()
}
""")
public class TestCommand2 {

  static final String THING_ARGUMENT = "thing";

  @VariableInitializer
  void initializeVars(Map<String, Object> variables) {
    variables.put("argument_name", THING_ARGUMENT);
  }

  public void run(CommandContext<CommandSource> context,
                  @Argument(THING_ARGUMENT) String input
  ) {
    context.getSource().sendMessage(input);
  }
}
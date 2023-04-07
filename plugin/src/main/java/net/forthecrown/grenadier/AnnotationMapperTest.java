package net.forthecrown.grenadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.types.EntitySelector;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CommandData("""
name = 'test_type_mapping'

argument('entity_arg', entity) {
  map_result = result.findEntity()
  
  executes = runEntity()
  
  literal('original_again') = runEntity1()
  
  argument('other_mapping', bool) {
    map_result('entity_arg') = result.findPlayer()
    executes = run3()
  }
}
""")
public class AnnotationMapperTest {

  public void runEntity(CommandContext<CommandSource> context,
                        @Argument("entity_arg") Entity entity
  ) {
    context.getSource().sendMessage(entity.toString());
  }

  public void runEntity1(CommandContext<CommandSource> context,
                         @Argument("entity_arg") EntitySelector selector
  ) throws CommandSyntaxException {
    context.getSource().sendMessage(
        selector.findEntity(context.getSource()).toString()
    );
  }

  public void run3(CommandContext<CommandSource> context,
                         @Argument("entity_arg") Player selector
  ) throws CommandSyntaxException {
    context.getSource().sendMessage(
        selector.toString()
    );
  }
}
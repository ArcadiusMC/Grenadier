package net.forthecrown.grenadier;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.annotations.CommandFile;

@CommandFile("transformer-test.gcn")
public class TransformerTestCommand {

  void execFunc(CommandSource source) {
    source.sendMessage("Hello, world!");
  }

  void transformerFunction(ArgumentBuilder<CommandSource, ?> node) {
    node.then(Nodes.literal("another-literal")
        .executes(c -> {
          c.getSource().sendMessage("Hello, again!");
          return 0;
        })
    );
  }
}

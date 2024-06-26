package net.forthecrown.grenadier;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;

public class CustomTypeFailTest extends AbstractCommand {

  public CustomTypeFailTest() {
    super("custom_type_fail");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("argument", ArgType1.INST)
            .executes(c -> {
              c.getSource().sendMessage("How are you here :(");
              return 0;
            })
        );
  }

  // This should fail because argtype1 is referencing a supposedly vanilla mapped type,
  // that is not actually mapped to vanilla
  enum ArgType1 implements ArgumentType<String>, SimpleVanillaMapped {
    INST;

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
      return reader.readString();
    }

    @Override
    public ArgumentType<?> getVanillaType() {
      return ArgType2.INST;
    }
  }

  enum ArgType2 implements ArgumentType<String> {
    INST;

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
      return reader.readString();
    }
  }
}

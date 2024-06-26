package net.forthecrown.grenadier;

public class VanillaOverrideTest extends AbstractCommand {

  public VanillaOverrideTest() {
    super("enchant");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      c.getSource().sendMessage("I was overriden :)");
      return 0;
    });
  }
}

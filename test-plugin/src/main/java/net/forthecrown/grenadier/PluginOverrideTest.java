package net.forthecrown.grenadier;

public class PluginOverrideTest extends AbstractCommand {

  public PluginOverrideTest() {
    super("abandonclaim");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      c.getSource().sendMessage("I was overridden :)");
      return 0;
    });
  }
}

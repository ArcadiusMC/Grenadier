package net.forthecrown.grenadier;

import org.bukkit.plugin.java.JavaPlugin;

public class GrenadierPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Grenadier.plugin(this);
    new TestCommand();
  }

  @Override
  public void onDisable() {

  }
}
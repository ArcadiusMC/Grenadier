package net.forthecrown.grenadier;

import org.bukkit.plugin.java.JavaPlugin;

public class GrenadierPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Grenadier.getProvider().setPlugin(this);
  }

  @Override
  public void onDisable() {

  }
}
package net.forthecrown.grenadier.internal;

import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GrenadierListener implements Listener {

  private final GrenadierProviderImpl provider;

  public GrenadierListener(GrenadierProviderImpl provider) {
    this.provider = provider;
  }

  @EventHandler(ignoreCancelled = true)
  public void onServerResourcesReloaded(ServerResourcesReloadedEvent event) {
    provider.reregisterAll();
  }

}

/**
 * Command engine made for PaperMC that uses Mojang's Brigadier.
 */
module grenadier {
  requires org.bukkit;
  requires paper.mojangapi;
  requires paper.server.userdev;
  requires brigadier;
  requires net.kyori.adventure;
  requires net.kyori.adventure.key;
  requires net.kyori.adventure.text.serializer.legacy;
  requires org.jetbrains.annotations;
  requires lombok;
  requires nbt;
  requires paper.nbt;

  exports net.forthecrown.grenadier;
  exports net.forthecrown.grenadier.types;
  exports net.forthecrown.grenadier.utils;
  exports net.forthecrown.grenadier.types.position;
}
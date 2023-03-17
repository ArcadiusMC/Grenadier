# Grenadier
Command engine made with Mojang's Brigadier for PaperMC
  
Allows for plugin developers to create commands using the same
command engine that the vanilla game uses. This makes command
creation and argument parsing a *lot* easier than is possible
with Bukkit's command system

## Documentation and guides:
Documentation and guides on how to use Grenadier are available
in the grenadier javadoc, located [here](https://www.javadoc.io/doc/net.forthecrown/grenadier/latest/index.html)

## Dependency info
```kotlin
repositories {
  mavenCentral()
  
  // Brigadier's repository
  maven("https://libraries.minecraft.net")
}

dependencies {
  // Grenadier itself
  implementation("net.forthecrown:grenadier:2.0.0")
  
  // Mojang's Brigadier engine
  compileOnly("com.mojang:brigadier:1.0.18")
}
```

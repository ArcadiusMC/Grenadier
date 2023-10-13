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
Kotlin: 
```kotlin
repositories {
  mavenCentral()
  
  // Brigadier's repository
  maven("https://libraries.minecraft.net")
}

dependencies {
  // Grenadier itself
  implementation("net.forthecrown:grenadier:2.2.0")
  
  // Get the annotation library with
  implementation("net.forthecrown:grenadier-annotations:1.3.0")
  
  // Mojang's Brigadier engine
  compileOnly("com.mojang:brigadier:1.0.18")
}
```
Maven:
```xml
<repositories>
  <!-- Repository for Brigadier -->
  <repository>
    <id>minecraft-libraries</id>
    <url>https://libraries.minecraft.net/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>net.forthecrown</groupId>
    <artifactId>grenadier</artifactId>
    <version>2.2.0</version>
  </dependency>

  <!-- Optional annotation library -->
  <dependency>
    <groupId>net.forthecrown</groupId>
    <artifactId>grenadier-annotations</artifactId>
    <version>1.3.0</version>
  </dependency>

  <!-- Brigadier itself -->
  <dependency>
    <groupId>com.mojang</groupId>
    <artifactId>brigadier</artifactId>
    <version>1.0.18</version>
  </dependency>
</dependencies>
```
**Ensuring Grenadier gets downloaded**  
If you're intending on shading Grenadier into your plugin, then you can ignore this part. This is intended for using the 
`config.yml` for Bukkit plugins and the `PluginLoader` for Paper plugins
  
### Bukkit plugins
Simply add this line to your `plugin.yml`
```yml
libraries:
  - "net.forthecrown:grenadier:2.2.0"

  # Optional annotations library
  - "net.forthecrown:grenadier-annotations:1.3.0"
```
### Paper plugins
You'll need to create an implementation of `PluginLoader`, example: 
```java
package me.loaderexample;

public class LoaderExample implements PluginLoader {
  
  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    MavenLibraryResolver resolver = new MavenLibraryResolver();

    resolver.addDependency(
      new Dependency(new DefaultArtifact("net.forthecrown:grenadier:2.2.0"), null)
    );
    
    // Optional annotations library
    resolver.addDependency(
      new Dependency(new DefaultArtifact("net.forthecrown:grenadier-annotations:1.3.0"), null)
    );

    // Add Maven central repository
    RemoteRepository repo = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
    resolver.addRepository(repo);

    classpathBuilder.addLibrary(resolver);
  }
}
```
You'll then need to tell Paper this loader exists by putting this line in your 
`paper-plugin.yml`: 
```yml
loader: "me.loaderexample.LoaderExample"
```

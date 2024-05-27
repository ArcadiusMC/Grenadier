package net.forthecrown.grenadier;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.nio.file.Path;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class TestPluginLoader implements PluginLoader {

  @Override
  public void classloader(@NotNull PluginClasspathBuilder builder) {
    Path grenadierDir = Path.of("grenadier");
    builder.addLibrary(new JarLibrary(grenadierDir.resolve("main.jar")));
    builder.addLibrary(new JarLibrary(grenadierDir.resolve("annotations.jar")));

    MavenLibraryResolver resolver = new MavenLibraryResolver();

    resolver.addRepository(
        new RemoteRepository.Builder("mavenCentral", "default", "https://repo1.maven.org/maven2/")
            .build()
    );

    resolver.addDependency(new Dependency(new DefaultArtifact("net.forthecrown:nbt:1.5.1"), null));
    resolver.addDependency(new Dependency(new DefaultArtifact("net.forthecrown:paper-nbt:1.7.1"), null));

    builder.addLibrary(resolver);
  }
}

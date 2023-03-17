plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.5.2"
}

group = "net.forthecrown"
version = "2.0.0"

repositories {
  mavenCentral()

  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://libraries.minecraft.net")
}

dependencies {
  // As a general rule, lombok is only used in implementation
  // classes, not in the API.
  compileOnly("org.projectlombok:lombok:1.18.22")
  annotationProcessor("org.projectlombok:lombok:1.18.22")

  api("com.mojang:brigadier:1.0.18")

  api("net.forthecrown:nbt:1.2.1")
  api("net.forthecrown:paper-nbt:1.2.2")

  paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
}

tasks {
  assemble {
    dependsOn(reobfJar)
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
  }

  java {
    withSourcesJar()
    withJavadocJar()
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()

    val docOptions: StandardJavadocDocletOptions
        = options as StandardJavadocDocletOptions;

    val links = docOptions.links!!;
    links.add("https://jd.papermc.io/paper/1.19/")
    links.add("https://jd.advntr.dev/api/4.13.0/")
    links.add("https://repo.karuslabs.com/repository/brigadier/")
    links.add("https://javadoc.io/doc/net.forthecrown/paper-nbt/latest/")
    links.add("https://javadoc.io/doc/net.forthecrown/nbt/latest/")

    exclude("net/forthecrown/grenadier/internal")
    exclude("net/forthecrown/grenadier/internal/*")
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
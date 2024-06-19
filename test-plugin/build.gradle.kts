plugins {
  `java-library`
  id("com.github.johnrengelman.shadow") version "8.0.0"
}

group = "net.forthecrown"
version = "2.2.0"

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://libraries.minecraft.net")
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

  compileOnly("com.mojang:brigadier:1.2.9")
  implementation(project(":grenadier"))
  implementation(project(":grenadier-annotations"))

  //compileOnly("net.forthecrown:grenadier:2.3.2:")

  compileOnly("net.forthecrown:nbt:1.5.2")
  compileOnly("net.forthecrown:paper-nbt:1.7.4")

  compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
}

tasks {
  test {
    useJUnitPlatform()
  }

  compileJava {
    options.release = 21
  }

  processResources {
    expand("version" to version)
  }

  shadowJar {
    exclude("net/forthecrown/nbt/**")
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
plugins {
  `java-library`
  id("com.github.johnrengelman.shadow") version "8.0.0"
}

group = "net.forthecrown"
version = "2.0.0"

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://libraries.minecraft.net")
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

  compileOnly("com.mojang:brigadier:1.0.500")
  implementation(project(":grenadier", "reobf"))
  implementation(project(":grenadier-annotations"))

  compileOnly("net.forthecrown:nbt:1.2.1")
  compileOnly("net.forthecrown:paper-nbt:1.2.2")

  compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
}

tasks {
  test {
    useJUnitPlatform()
  }

  processResources {
    expand("version" to version)
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
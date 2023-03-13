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
  compileOnly("org.projectlombok:lombok:1.18.22")
  annotationProcessor("org.projectlombok:lombok:1.18.22")

  testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
  compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

  compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
  compileOnly("io.papermc.paper:paper-mojangapi:1.19.3-R0.1-SNAPSHOT")
  compileOnly("com.mojang:brigadier:1.0.18")

  compileOnly("net.forthecrown:nbt:1.2.1")
  compileOnly("net.forthecrown:paper-nbt:1.2.2")

  paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT")
}

tasks {
  test {
    useJUnitPlatform()
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.7.1"

  // Maven publishing
  id("maven-publish")
  id("signing")
}

group = "net.forthecrown"
version = "2.6.0"

repositories {
  mavenCentral()

  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://libraries.minecraft.net")
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

  // As a general rule, lombok is only used in implementation
  // classes, not in the API.
  compileOnly("org.projectlombok:lombok:1.18.32")
  annotationProcessor("org.projectlombok:lombok:1.18.32")

  compileOnly("com.mojang:brigadier:1.2.9")

  api("net.forthecrown:nbt:1.5.2")
  api("net.forthecrown:paper-nbt:1.8.0")

  paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release = 21
  }

  java {
    withSourcesJar()
    withJavadocJar()
  }

  test {
    useJUnitPlatform()
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()

    val docOptions: StandardJavadocDocletOptions
        = options as StandardJavadocDocletOptions

    val links = docOptions.links!!
    links.add("https://jd.papermc.io/paper/1.19/")
    links.add("https://jd.advntr.dev/api/4.13.0/")
    //links.add("https://repo.karuslabs.com/repository/brigadier/")
    links.add("https://javadoc.io/doc/net.forthecrown/paper-nbt/latest/")
    links.add("https://javadoc.io/doc/net.forthecrown/nbt/latest/")

    exclude("**/internal/**")
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        name.set("grenadier")
        description.set("Command engine made with Mojang's Brigadier for PaperMC")
        url.set("https://github.com/ArcadiusMC/Grenadier")

        licenses {
          license {
            name.set("MIT License")
            url.set("https://raw.githubusercontent.com/ArcadiusMC/Grenadier/main/LICENSE")
          }
        }

        developers {
          developer {
            name.set("JulieWoolie")
            id.set("JulieWoolie")
          }
        }

        scm {
          connection.set("scm:git:git:github.com/ArcadiusMC/Grenadier/.git")
          developerConnection.set("scm:git:ssh://github.com/ArcadiusMC/Grenadier/.git")
          url.set("https://github.com/ArcadiusMC/Grenadier")
        }
      }
    }
  }

  repositories {
    maven {
      name = "OSSRH"
      url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = project.properties["ossrhUsername"].toString()
        password = project.properties["ossrhPassword"].toString()
      }
    }
  }
}

signing {
  sign(publishing.publications["maven"])
}
plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.5.3"

  // Maven publishing
  id("maven-publish")
  id("signing")
}

group = "net.forthecrown"
version = "2.0.7"

repositories {
  mavenCentral()

  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://libraries.minecraft.net")
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

  // As a general rule, lombok is only used in implementation
  // classes, not in the API.
  compileOnly("org.projectlombok:lombok:1.18.22")
  annotationProcessor("org.projectlombok:lombok:1.18.22")

  api("com.mojang:brigadier:1.0.18")

  api("net.forthecrown:nbt:latest.release")
  api("net.forthecrown:paper-nbt:latest.release")

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

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        name.set("grenadier")
        description.set("Command engine made with Mojang's Brigadier for PaperMC")
        url.set("https://github.com/ForTheCrown/Grenadier")

        // For some reason the normal jar file gets forgotten lol,
        // so gotta do this to include it
        val jarPath = buildDir.path + "/libs/${project.name}-${project.version}.jar"
        val jarFile = file(jarPath)
        artifact(jarFile)

        licenses {
          license {
            name.set("MIT License")
            url.set("https://raw.githubusercontent.com/ForTheCrown/Grenadier/main/LICENSE")
          }
        }

        developers {
          developer {
            name.set("JulieWoolie")
            id.set("JulieWoolie")
          }
        }

        scm {
          connection.set("scm:git:git:github.com/ForTheCrown/Grenadier/.git")
          developerConnection.set("scm:git:ssh://github.com/ForTheCrown/Grenadier/.git")
          url.set("https://github.com/ForTheCrown/Grenadier")
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
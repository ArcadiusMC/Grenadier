plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "1.5.5"

  // Maven publishing
  id("maven-publish")
  id("signing")
}

group = "net.forthecrown"
version = "2.1.2"

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

  compileOnly("com.mojang:brigadier:1.0.18")

  api("net.forthecrown:nbt:1.4.0")
  api("net.forthecrown:paper-nbt:1.4.0")

  paperweight.paperDevBundle("1.20-R0.1-SNAPSHOT")
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

    exclude("**/internal/**")
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      // Apparently, the output of the reobfJar task is the `-dev` jar, not the remapped jar
      // So do this hack to include 2 extra jar files, the remapped jar itself with no extension
      // and a '-reobf' jar that can be shaded without the '-dev' jar interfering
      //
      // Man, I wish I wasn't this dumb
      //
      artifact("build/libs/${project.name}-$version.jar")
      artifact("build/libs/${project.name}-$version.jar") {
        classifier = "reobf"
        extension = "jar"
      }

      pom {
        name.set("grenadier")
        description.set("Command engine made with Mojang's Brigadier for PaperMC")
        url.set("https://github.com/ForTheCrown/Grenadier")

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
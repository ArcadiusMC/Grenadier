plugins {
  `java-library`

  // Maven publishing
  id("maven-publish")
  id("signing")
}

group = "net.forthecrown"
version = "1.3.3"

repositories {
  mavenCentral()

  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://libraries.minecraft.net")
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
  testImplementation("com.google.guava:guava:31.1-jre")

  compileOnly(project(":grenadier"))

  compileOnly("com.mojang:brigadier:1.2.9")
  compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")

  compileOnly("org.projectlombok:lombok:1.18.32")
  annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks {
  test {
    useJUnitPlatform()
  }

  compileJava {
    options.release = 21
  }

  java {
    withSourcesJar()
    withJavadocJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
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
    links.add("https://javadoc.io/doc/net.forthecrown/grenadier/latest/")

    exclude("**/compiler/**")
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])

      pom {
        name.set("grenadier-annotations")
        description.set("Annotation-based command support for Grenadier")
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
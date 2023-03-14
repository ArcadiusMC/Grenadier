plugins {
  `java-library`
}

group = "net.forthecrown"
version = "2.0.0"

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")

  implementation(project(":grenadier"))

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
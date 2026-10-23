plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")

    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.github.networkminesky.mainframe:mainframe-api:578767c23e")

    compileOnly("org.geysermc.geyser:api:2.11.2-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        filesMatching("velocity-plugin.json") {
            expand(props)
        }
    }
}

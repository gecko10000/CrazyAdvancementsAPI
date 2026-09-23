import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("maven-publish")
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

group = "eu.endercentral.crazy_advancements"
version = "26.3.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("26.3.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
        dependsOn(clean)
    }

    jar.get().archiveFileName = "${name}-${version}-mojmap.jar"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

paper {
    name = "CrazyAdvancementsAPI"
    main = "eu.endercentral.crazy_advancements.CrazyAdvancementsAPI"
    apiVersion = "26.3"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("maven-publish")
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

group = "eu.endercentral.crazy_advancements"
version = "26.2.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
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
    apiVersion = "26.2"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

/*bukkit {
    main = "eu.endercentral.crazy_advancements.CrazyAdvancementsAPI"
    author = "ZockerAxel"
    apiVersion = "1.20.5" // Should be always same as dev bundle version
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    commands {
        register("grant") {
            usage = "/grant <Player> <Manager> <Advancement> [Criteria...]"
            description = "Grants <Advancement>-[Criteria...] to <Player> in <Manager>"
            aliases = listOf("cagrant")
        }

        register("revoke") {
            usage = "/revoke <Player> <Manager> <Advancement> [Criteria...]"
            description = "Revokes <Advancement>-[Criteria...] to <Player> in <Manager>"
            aliases = listOf("carevoke")
        }

        register("setprogress") {
            usage = "/setprogress <Player> <Manager> <Advancement> <Number> [Operation]"
            description = "Sets <Advancement> Progress for <Player> in <Manager> using [Operation]"
            aliases = listOf("caprogress")
        }

        register("showtoast") {
            usage = "/showtoast <Player> <Icon> [Frame] <Message>"
            description = "Displays a Toast Advancement Message"
            aliases = listOf("catoast", "toast")
        }

        register("careload") {
            usage = "/careload [Category]"
            description = "Reloads the Crazy Advancements API. Valid categories are all, advancements, items"
        }
    }
}*/

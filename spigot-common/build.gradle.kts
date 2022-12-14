
dependencies {
    testImplementation(testFixtures(projects.core))

    // 1.8.8 is supported but we target 1.9.4 to use PlayerSwapItemEvent if on 1.9.4 or above
    compileOnly("org.spigotmc:spigot-api:1.9.4-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.5.21") // Mirrored from floodgate-spigot - probably the version from 1.8.8
    compileOnly("me.clip:placeholderapi:2.11.2")
    api(projects.core)
    api(projects.accessItem)
    api("cloud.commandframework:cloud-paper:1.8.0")
    api("me.lucko:commodore:2.2")
    api("net.kyori:adventure-platform-bukkit:4.2.0")
    api("org.bstats:bstats-bukkit:3.0.0")
}

description = "spigot-common"

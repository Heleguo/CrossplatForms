package dev.kejona.crossplatforms.spigot;

import dev.kejona.crossplatforms.utils.ReflectionUtils;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Thanks to Floodgate
 * https://github.com/GeyserMC/Floodgate/blob/master/spigot/src/main/java/org/geysermc/floodgate/util/ClassNames.java
 */
public final class ClassNames {

    /**
     * Includes the v at the front
     */
    public static final String NMS_VERSION;
    private static final String CRAFTBUKKIT_PACKAGE;

    public static final Method PLAYER_GET_PROFILE;
    public static final Field META_SKULL_PROFILE;

    private static final Map<String, String> NMS_VERSIONS = new HashMap<String, String>() {
        {
            put("1.21", "v1_21_R1");
            put("1.21.1", "v1_21_R1");
            put("1.21.2", "v1_21_R2");
            put("1.21.3", "v1_21_R2");
            put("1.21.4", "v1_21_R3");
            put("1.21.5", "v1_21_R3");
            put("1.21.6", "v1_21_R3");
            put("1.21.7", "v1_21_R3");
            put("1.21.8", "v1_21_R3");
            put("1.21.9", "v1_21_R3");
        }
    };

    static {
        System.out.println(Bukkit.getServer().getBukkitVersion().split("-")[0]);
        String nmsVersion = null;
        try {
            nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            // 如果在這裡發生錯誤，則使用 NMS_VERSIONS 中對應的版本
            String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
            nmsVersion = NMS_VERSIONS.get(bukkitVersion);
        }

        // If nmsVersion is still null, try to get it from the map based on bukkit
        // version
        if (nmsVersion == null) {
            String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
            nmsVersion = NMS_VERSIONS.get(bukkitVersion);
        }

        // If still null, default to the latest known version
        if (nmsVersion == null) {
            nmsVersion = "v1_21_R3";
            System.out.println("Warning: Could not determine NMS version, defaulting to " + nmsVersion);
        }

        NMS_VERSION = nmsVersion;
        System.out.println(NMS_VERSION);
        CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + NMS_VERSION;

        Class<?> craftPlayer = null;
        Method playerGetProfile = null;
        Class<?> craftMetaSkull = null;
        Field metaSkullProfile = null;

        try {
            // Try with versioned package first
            craftPlayer = ReflectionUtils.getClass(CRAFTBUKKIT_PACKAGE + ".entity.CraftPlayer");
            if (craftPlayer != null) {
                playerGetProfile = ReflectionUtils.getMethod(craftPlayer, "getProfile");
            }

            craftMetaSkull = ReflectionUtils.getClass(CRAFTBUKKIT_PACKAGE + ".inventory.CraftMetaSkull");
            if (craftMetaSkull != null) {
                metaSkullProfile = ReflectionUtils.getField(craftMetaSkull, "profile");
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not find versioned CraftBukkit classes, trying fallback...");
        }

        // Fallback for newer versions that might not use versioned packages
        if (craftPlayer == null) {
            try {
                // Try without version (for newer MC versions)
                craftPlayer = ReflectionUtils.getClass("org.bukkit.craftbukkit.entity.CraftPlayer");
                if (craftPlayer != null) {
                    playerGetProfile = ReflectionUtils.getMethod(craftPlayer, "getProfile");
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not find CraftPlayer class in any package");
            }
        }

        if (craftMetaSkull == null) {
            try {
                // Try without version (for newer MC versions)
                craftMetaSkull = ReflectionUtils.getClass("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
                if (craftMetaSkull != null) {
                    metaSkullProfile = ReflectionUtils.getField(craftMetaSkull, "profile");
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not find CraftMetaSkull class in any package");
            }
        }

        PLAYER_GET_PROFILE = playerGetProfile;
        META_SKULL_PROFILE = metaSkullProfile;
    }
}

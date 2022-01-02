package dev.projectg.crossplatforms;

import dev.projectg.crossplatforms.command.MainCommand;
import dev.projectg.crossplatforms.config.ConfigManager;
import dev.projectg.crossplatforms.form.AccessItemRegistry;
import dev.projectg.crossplatforms.form.InventoryManager;
import dev.projectg.crossplatforms.form.java.JavaMenuListeners;
import dev.projectg.crossplatforms.form.bedrock.BedrockFormRegistry;
import dev.projectg.crossplatforms.form.java.JavaMenuRegistry;
import dev.projectg.crossplatforms.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class CrossplatForms extends JavaPlugin {
    private static CrossplatForms plugin;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        plugin = this;
        // getting the logger forces the config to load before our loadConfiguration() is called...
        Logger logger = Logger.getLogger();

        try {
            Properties gitProperties = new Properties();
            gitProperties.load(FileUtils.getResource("git.properties"));
            logger.info("Branch: " + gitProperties.getProperty("git.branch", "Unknown") + ", Commit: " + gitProperties.getProperty("git.commit.id.abbrev", "Unknown"));
        } catch (IOException | NullPointerException | IllegalArgumentException e) {
            logger.warn("Unable to load resource: git.properties");
            e.printStackTrace();
        }

        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.warn("This plugin works best with PlaceholderAPI! Since you don't have it installed, only %player_name% and %player_uuid% will work in the GeyserHub config!");
        }

        configManager = new ConfigManager();
        if (!configManager.loadAllConfigs()) {
            logger.severe("Disabling due to configuration error.");
            return;
        }

        // Bungee channel for selector
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Load forms
        AccessItemRegistry accessItemRegistry = new AccessItemRegistry();
        BedrockFormRegistry bedrockFormRegistry = new BedrockFormRegistry();
        JavaMenuRegistry javaMenuRegistry = new JavaMenuRegistry();

        Objects.requireNonNull(getCommand("crossplatforms")).setExecutor(new MainCommand(bedrockFormRegistry, javaMenuRegistry));

        // Listeners for the Bedrock and Java menus
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryManager(accessItemRegistry, bedrockFormRegistry, javaMenuRegistry), this);
        Bukkit.getServer().getPluginManager().registerEvents(new JavaMenuListeners(javaMenuRegistry), this);

        logger.info("Took " + (System.currentTimeMillis() - start) + "ms to boot CrossplatForms.");
    }

    public static CrossplatForms getInstance() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
package dev.projectg.geyserhub.utils.bstats;

import dev.projectg.geyserhub.GeyserHubMain;

public class SelectorLogger implements Reloadable {

    private static final SelectorLogger LOGGER = new SelectorLogger(GeyserHubMain.getInstance());

    private final GeyserHubMain plugin;
    private boolean debug;

    public static SelectorLogger getLogger() {
        return LOGGER;
    }

    private SelectorLogger(GeyserHubMain plugin) {
        this.plugin = plugin;
        debug = plugin.getConfig().getBoolean("Enable-Debug", false);
        ReloadableRegistry.registerReloadable(this);
    }

    public void info(String message) {
        plugin.getLogger().info(message);
    }
    public void warn(String message) {
        plugin.getLogger().warning(message);
    }
    public void severe(String message) {
        plugin.getLogger().severe(message);
    }
    public void debug(String message) {
        if (debug) {
            plugin.getLogger().info(message);
        }
    }

    @Override
    public boolean reload() {
        debug = plugin.getConfig().getBoolean("Enable-Debug", false);
        return true;
    }
}
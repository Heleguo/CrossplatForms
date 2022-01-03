package dev.projectg.crossplatforms.form.java;

import dev.projectg.crossplatforms.CrossplatForms;
import dev.projectg.crossplatforms.config.mapping.java.MenuConfig;
import dev.projectg.crossplatforms.config.mapping.java.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class JavaMenuListeners implements Listener {

    private final JavaMenuRegistry javaMenuRegistry;

    public JavaMenuListeners(@Nonnull JavaMenuRegistry javaMenuRegistry) {
        this.javaMenuRegistry = Objects.requireNonNull(javaMenuRegistry);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // This is used for processing inventory clicks WITHIN the java menu GUI

        if (!CrossplatForms.getInstance().getConfigManager().getConfig(MenuConfig.class).isEnable()) {
            return;
        }

        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();

            if (item != null) {
                Menu menu = javaMenuRegistry.getMenu(item);
                if (menu != null) {
                    event.setCancelled(true);
                    menu.process(event.getSlot(), event.isRightClick(), player);
                }
            }
        }
    }
}
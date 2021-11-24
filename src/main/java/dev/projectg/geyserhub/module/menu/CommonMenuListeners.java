package dev.projectg.geyserhub.module.menu;

import dev.projectg.geyserhub.module.menu.bedrock.BedrockFormRegistry;
import dev.projectg.geyserhub.module.menu.java.JavaMenuRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CommonMenuListeners implements Listener {

    private final AccessItemRegistry accessItemRegistry;
    private final BedrockFormRegistry bedrockFormRegistry;
    private final JavaMenuRegistry javaMenuRegistry;

    public CommonMenuListeners(AccessItemRegistry accessItemRegistry, BedrockFormRegistry bedrockFormRegistry, JavaMenuRegistry javaMenuRegistry) {
        this.accessItemRegistry = accessItemRegistry;
        this.bedrockFormRegistry = bedrockFormRegistry;
        this.javaMenuRegistry = javaMenuRegistry;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) { // opening menus through access items
        if (!accessItemRegistry.isEnabled()) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null) {
                AccessItem accessItem = accessItemRegistry.getAccessItem(item);
                if (accessItem != null) {
                    event.setCancelled(true); // todo: what happens if we don't cancel this? does the chest open before or after ours?

                    Player player = event.getPlayer();
                    String formName = accessItem.formName;
                    MenuUtils.sendForm(player, bedrockFormRegistry, javaMenuRegistry, formName);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) { // keep the access items in place
        if (!accessItemRegistry.isEnabled()) {
            return;
        }

        // todo: don't allow duplication for creative players
        ItemStack item = event.getCurrentItem();
        if (item != null) {
            AccessItem accessItem = accessItemRegistry.getAccessItem(item);
            if (accessItem != null) {
                event.setCancelled(!accessItem.allowMove);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) { // don't let the access item be dropped, destroy it if it is
        if (!accessItemRegistry.isEnabled()) {
            return;
        }

        ItemStack item = event.getItemDrop().getItemStack();
        AccessItem accessItem = accessItemRegistry.getAccessItem(item);
        if (accessItem != null) {
            if (!accessItem.allowDrop) {
                event.setCancelled(true);
            } else if (accessItem.destroyDropped) {
                event.getItemDrop().remove();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) { // give the access item when the player joins
        if (!accessItemRegistry.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        // Remove any access items that are already in the inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            if (AccessItemRegistry.getAccessItemId(item) != null) {
                // Even if this specific item/access item is no longer registered
                // The fact it has the ID inside of it means it once was or still is
                player.getInventory().remove(item);
            }
        }

        boolean setHeldSlot = false; // If we have changed the item being held
        for (AccessItem accessItem : accessItemRegistry.getAccessItems().values()) {
            if (accessItem.onJoin) {
                ItemStack accessItemStack = accessItem.getItemStack(player); // todo update placeholders after the fact. but when?

                int desiredSlot = accessItem.slot;
                ItemStack oldItem = player.getInventory().getItem(desiredSlot);
                boolean success = false;
                if (oldItem == null || oldItem.getType() == Material.AIR) {
                    // put the item in the desired place
                    player.getInventory().setItem(desiredSlot, accessItemStack);
                    success = true;
                } else {
                    // find somewhere else to put it in the hotbar
                    for (int i = 0; i < 10 && i != desiredSlot; i++) {
                        if (player.getInventory().getItem(i) == null || Objects.requireNonNull(player.getInventory().getItem(i)).getType() == Material.AIR) {
                            player.getInventory().setItem(i, oldItem);
                            player.getInventory().setItem(desiredSlot, accessItemStack);
                            success = true;
                            break;
                        }
                    }
                    // If the player doesn't have the space in their hotbar then they don't get it
                }
                if (success && !setHeldSlot) {
                    // Set the held item to the first access item
                    event.getPlayer().getInventory().setHeldItemSlot(accessItem.slot);
                    setHeldSlot = true;
                }
            }
        }
    }
}
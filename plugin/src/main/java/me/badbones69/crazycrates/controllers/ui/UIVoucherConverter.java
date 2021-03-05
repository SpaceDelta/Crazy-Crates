package me.badbones69.crazycrates.controllers.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.badbones69.crazycrates.Main;
import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class UIVoucherConverter implements Listener {

    private static final int BUTTON_CANCEL = 48, BUTTON_CONFIRM = 50, BUTTON_INFO = 49;
    private static final int[] BUTTON_BORDER = new int[]{45, 46, 47, 51, 52, 53};

    private final Player player;
    private Inventory inventory;

    private boolean selectedOption;

    public UIVoucherConverter(Player player) {
        Bukkit.getServer().getPluginManager().registerEvents(this, Main.INSTANCE);

        this.player = player;
        inventory = Bukkit.getServer().createInventory(null, 54,
                ChatColor.GREEN + ChatColor.BOLD.toString() + "Convert Physical Crate Keys");

        // add buttons

        // border
        // cancel
        ItemStack cancel = new ItemBuilder()
                .setMaterial(Material.RED_STAINED_GLASS_PANE)
                .setName(ChatColor.RED + "Cancel")
                .build();
        inventory.setItem(BUTTON_CANCEL, cancel);

        // confirm
        ItemStack confirm = new ItemBuilder()
                .setMaterial(Material.GREEN_STAINED_GLASS_PANE)
                .setName(ChatColor.GREEN + "Confirm")
                .build();
        inventory.setItem(BUTTON_CONFIRM, confirm);

        // info button
        ItemStack infoButton = new ItemBuilder()
                .setMaterial(Material.REDSTONE_TORCH)
                .setName(ChatColor.GREEN + "Place some old physical crate keys in here...")
                .addLore(ChatColor.GRAY + "In return you'll get them back in virtual form!")
                .build();
        inventory.setItem(BUTTON_INFO, infoButton);

        // border
        ItemStack border = new ItemBuilder()
                .setMaterial(Material.GRAY_STAINED_GLASS_PANE)
                .setName(ChatColor.GRAY.toString())
                .build();
        for (int borderSlot : BUTTON_BORDER) {
            inventory.setItem(borderSlot, border);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (player.equals(event.getWhoClicked())
                && (event.getClickedInventory() == null || inventory.equals(event.getClickedInventory()))) {

            if (event.getRawSlot() < 44)
                return;

            event.setCancelled(true);

            if (event.getRawSlot() == BUTTON_CANCEL) {
                selectedOption = true;
                player.closeInventory();

                completeOperation(false);
                return;
            }

            if (event.getRawSlot() == BUTTON_CONFIRM) {
                selectedOption = true;
                player.closeInventory();

                completeOperation(true);
            }
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (player.equals(event.getPlayer())) {
            HandlerList.unregisterAll(this);

            if (!selectedOption)
                completeOperation(false);
        }
    }

    private void completeOperation(boolean accepted) {
        List<ItemStack> returnItems;

        final List<ItemStack> submittedItems = getSubmittedItems();

        if (accepted) {
            returnItems = Lists.newArrayList();

            final CrazyCrates instance = CrazyCrates.getInstance();
            int validKeys = 0;

            Map<Crate, Integer> keysToGive = Maps.newHashMap();

            // itr through items
            for (ItemStack item : submittedItems) {
                final Crate crateFromKey = instance.getCrateFromKey(item);

                if (crateFromKey == null) {
                    returnItems.add(item);
                    continue;
                }

                keysToGive.put(crateFromKey, keysToGive.getOrDefault(crateFromKey, 0) + item.getAmount());
                validKeys++;
            }

            // give keys
            if (validKeys > 0) {
                player.sendMessage(Methods.getPrefix("You have received:"));

                keysToGive.forEach((crate, amount) -> {
                    instance.addKeys(amount, player, crate, KeyType.VIRTUAL_KEY);
                    player.sendMessage(ChatColor.GREEN.toString() + "x" + amount + " " + crate.getName() + " Virtual Keys");
                });

                keysToGive.clear();
            }

            if (!returnItems.isEmpty()) {
                // tell invalid
                player.sendMessage(Methods.getPrefix(ChatColor.RED.toString() + returnItems.size() + " items were invalid keys so were returned."));
            }

        } else {
            returnItems = submittedItems;

            player.sendMessage(Methods.getPrefix(ChatColor.RED + "Key conversion cancelled, your items were returned to you."));
        }

        returnItems.forEach(itemStack -> player.getInventory().addItem(itemStack));
        inventory = null;
    }

    private List<ItemStack> getSubmittedItems() {
        List<ItemStack> items = Lists.newArrayList();
        for (int i = 0; i < BUTTON_BORDER[0]; i++) {
            final ItemStack item = inventory.getItem(i);
            if (item != null)
                items.add(item);
        }

        return items;
    }

}

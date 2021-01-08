package me.badbones69.crazycrates.controllers.ui;

import com.google.common.collect.Maps;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.badbones69.crazycrates.Main;
import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.FileManager.Files;
import me.badbones69.crazycrates.api.enums.CrateType;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.enums.Messages;
import me.badbones69.crazycrates.api.objects.Crate;
import me.badbones69.crazycrates.api.objects.ItemBuilder;
import me.badbones69.crazycrates.controllers.Preview;
import me.badbones69.crazycrates.controllers.ui.Button;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UICrateMenu implements Listener {

    private final Main main;
    private final CrazyCrates plugin;

    private final Map<Integer, Button> buttons;
    private final Map<UUID, Inventory> viewers;

    private final String inventoryTitle;
    private final int inventorySize;

    public UICrateMenu(Main main) {
        this.main = main;
        this.plugin = CrazyCrates.getInstance();
        this.buttons = Maps.newHashMap();
        this.viewers = Maps.newHashMap();

        // load contents
        final FileConfiguration config = Files.CONFIG.getFile();
        inventoryTitle = Methods.sanitizeColor(config.getString("Settings.InventoryName"));
        inventorySize = config.getInt("Settings.InventorySize");

        this.plugin.getCrates().forEach(crate -> {
            FileConfiguration file = crate.getFile();
            if (file == null || !file.getBoolean("Crate.InGUI"))
                return;

            String path = "Crate.";
            int slot = file.getInt(path + "Slot") - 1;
            if (slot + 1 > inventorySize)
                return;

            // add button
            buttons.put(slot, new Button() {
                @Override
                public ItemStack getItemStack(Player viewer) {
                    return new ItemBuilder()
                            .setMaterial(file.getString(path + "Item"))
                            .setName(file.getString(path + "Name"))
                            .setLore(file.getStringList(path + "Lore"))
                            .setPlayer(file.getString(path + "Player"))
                            .setGlowing(file.getBoolean(path + "Glowing"))
                            .addLorePlaceholder("%Keys%", NumberFormat.getNumberInstance().format(plugin.getVirtualKeys(viewer, crate)))
                            .addLorePlaceholder("%Keys_Physical%", NumberFormat.getNumberInstance().format(plugin.getPhysicalKeys(viewer, crate)))
                            .addLorePlaceholder("%Keys_Total%", NumberFormat.getNumberInstance().format(plugin.getTotalKeys(viewer, crate)))
                            .addLorePlaceholder("%Player%", viewer.getName())
                            .build();
                }

                @Override
                public void onAction(Player clicker, ClickType clickType) {

                    // check button
                    if (clickType == ClickType.RIGHT) {
                        if (crate.isPreviewEnabled()) {
                            clicker.closeInventory();
                            Preview.setPlayerInMenu(clicker, true);
                            Preview.openNewPreview(clicker, crate);
                        } else {
                            clicker.sendMessage(Messages.PREVIEW_DISABLED.getMessage());
                        }

                        return;
                    }

                    // check if already opening
                    if (plugin.isInOpeningList(clicker)) {
                        clicker.sendMessage(Messages.CRATE_ALREADY_OPENED.getMessage());
                        return;
                    }

                    // check if can use this key type
                    boolean hasKey = false;
                    KeyType keyType = KeyType.VIRTUAL_KEY;
                    if (plugin.getVirtualKeys(clicker, crate) >= 1) {
                        hasKey = true;
                    } else {
                        if (config.getBoolean("Settings.Virtual-Accepts-Physical-Keys")
                                && plugin.hasPhysicalKey(clicker, crate, false)) {
                            hasKey = true;
                            keyType = KeyType.PHYSICAL_KEY;
                        }
                    }

                    // check keys
                    if (!hasKey) {
                        if (file.contains("Settings.Need-Key-Sound")) {
                            Sound sound = Sound.valueOf(file.getString("Settings.Need-Key-Sound"));
                            clicker.playSound(clicker.getLocation(), sound, 1f, 1f);
                        }

                        clicker.sendMessage(Messages.NO_VIRTUAL_KEY.getMessage());
                        return;
                    }

                    // check worlds
                    for (String world : getDisabledWorlds()) {
                        if (world.equalsIgnoreCase(clicker.getWorld().getName())) {
                            clicker.sendMessage(Messages.WORLD_DISABLED.getMessage("%World%", clicker.getWorld().getName()));
                            return;
                        }
                    }

                    // check inv
                    if (Methods.isInventoryFull(clicker)) {
                        clicker.sendMessage(Messages.INVENTORY_FULL.getMessage());
                        return;
                    }

                    // open crate
                    plugin.openCrate(clicker, crate, keyType, clicker.getLocation(), true, false);

                    if (crate.getCrateType() == CrateType.VIRTUAL_ON_THE_GO)
                        updateSlot(clicker, slot);
                }
            });

        });

        /* fk this
        if (config.isConfigurationSection("Settings.GUI-Customizer"))


        final ConfigurationSection section = config.getConfigurationSection("Settings.GUI-Customizer");
        section.getKeys(false).forEach(rawSlot -> {
            int slot = Integer.parseInt(rawSlot);

            ItemBuilder item = new ItemBuilder();
            String data = config.getString("Settings.GUI-Customize." + rawSlot);
            for (String option : data.split(", ")) {

                if (option.contains("Item:")) {
                    item.setMaterial(option.replace("Item:", ""));
                }

            }

        });


        // add custom items
        for (String custom : Files.CONFIG.getFile().getStringList("Settings.GUI-Customizer")) {
            String[] split = custom.split(", ");
            ItemBuilder item = new ItemBuilder();

            int slot;
            for (String option : split) {
                if (option.contains("Item:")) {
                    item.setMaterial(option.replace("Item:", ""));
                }

                if (option.contains("Name:")) {
                    option = option.replace("Name:", "");
                    for (Crate crate : plugin.getCrates()) {
                        if (crate.getCrateType() != CrateType.MENU) {
                            option = option.replaceAll("%" + crate.getName().toLowerCase() + "%", plugin.getVirtualKeys(viewer, crate) + "")
                                    .replaceAll("%" + crate.getName().toLowerCase() + "_physical%", plugin.getPhysicalKeys(viewer, crate) + "")
                                    .replaceAll("%" + crate.getName().toLowerCase() + "_total%", plugin.getTotalKeys(viewer, crate) + "");
                        }
                    }

                    item.setName(option.replaceAll("%player%", viewer.getName()));
                }
                if (option.contains("Lore:")) {
                    option = option.replace("Lore:", "");
                    String[] d = option.split(",");
                    for (String l : d) {
                        for (Crate crate : plugin.getCrates()) {
                            if (crate.getCrateType() != CrateType.MENU) {
                                option = option.replaceAll("%" + crate.getName().toLowerCase() + "%", plugin.getVirtualKeys(viewer, crate) + "")
                                        .replaceAll("%" + crate.getName().toLowerCase() + "_physical%", plugin.getPhysicalKeys(viewer, crate) + "")
                                        .replaceAll("%" + crate.getName().toLowerCase() + "_total%", plugin.getTotalKeys(viewer, crate) + "");
                            }
                        }
                        item.addLore(option.replaceAll("%player%", viewer.getName()));
                    }
                }
                if (option.contains("Glowing:")) {
                    item.setGlowing(Boolean.parseBoolean(option.replace("Glowing:", "")));
                }
                if (option.contains("Player:")) {
                    item.setPlayer(option.replaceAll("%player%", viewer.getName()));
                }

                if (option.contains("Slot:")) {
                    slot = Integer.parseInt(option.replace("Slot:", "")) - 1;
                }

                if (option.contains("Unbreakable-Item")) {
                    item.setUnbreakable(Boolean.parseBoolean(option.replace("Unbreakable-Item:", "")));
                }
                if (option.contains("Hide-Item-Flags")) {
                    item.hideItemFlags(Boolean.parseBoolean(option.replace("Hide-Item-Flags:", "")));
                }
            }



            buttons.put(slot, new Button() {
                @Override
                public ItemStack getItemStack(Player viewer) {
                    return new ItemBuilder(item)
                            .addLorePlaceholder("%Keys%", NumberFormat.getNumberInstance().format(plugin.getVirtualKeys(viewer, crate)))
                            .addLorePlaceholder("%Keys_Physical%", NumberFormat.getNumberInstance().format(plugin.getPhysicalKeys(viewer, crate)))
                            .addLorePlaceholder("%Keys_Total%", NumberFormat.getNumberInstance().format(plugin.getTotalKeys(viewer, crate)))
                            .addLorePlaceholder("%Player%", viewer.getName())
                            .build();
                }

                @Override
                public void onAction(Player clicker, ClickType clickType) {

                }
            });
        }
         */

        // add filler
        if (config.getBoolean("Settings.Filler.Toggle", false)) {
            String id = Files.CONFIG.getFile().getString("Settings.Filler.Item");
            String name = Files.CONFIG.getFile().getString("Settings.Filler.Name");
            List<String> lore = Files.CONFIG.getFile().getStringList("Settings.Filler.Lore");
            ItemStack fillerItem = new ItemBuilder()
                    .setMaterial(id)
                    .setName(name)
                    .setLore(lore)
                    .build();

            for (int i = 0; i < inventorySize; i++) {
                if (!buttons.containsKey(i))
                    buttons.put(i, new Button() {
                        @Override
                        public ItemStack getItemStack(Player viewer) {
                            return fillerItem;
                        }

                        @Override
                        public void onAction(Player clicker, ClickType clickType) {
                        }
                    });
            }
        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player
                && event.getInventory().getType() == InventoryType.CHEST
                && event.getCurrentItem() != null && event.getCursor() != null
                && event.getClickedInventory() != null) {

            if (viewers.containsKey(event.getWhoClicked().getUniqueId())) {
                event.setCancelled(true);

                final Button button = buttons.get(event.getRawSlot());
                if (button != null) {
                    button.onAction((Player) event.getWhoClicked(), event.getClick());
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
    }

    public void open(Player player) {
        if (player.getOpenInventory().getTopInventory() != null) {
            player.closeInventory();

            Bukkit.getServer().getScheduler().runTaskLater(main, () -> {
                Inventory inventory = this.build(player);
                viewers.put(player.getUniqueId(), inventory);
            }, 1L);

            return;
        }

        Inventory inventory = build(player);
        viewers.put(player.getUniqueId(), inventory);
    }

    private Inventory build(Player player) {
        if (this.viewers.containsKey(player.getUniqueId())) {
            Inventory inventory = this.viewers.get(player.getUniqueId());
            inventory.clear();
            this.viewers.remove(player.getUniqueId());
        }

        Inventory inventory = Bukkit.getServer().createInventory(null, this.inventorySize, this.inventoryTitle);
        this.buttons.forEach((integer, button) -> inventory.setItem(integer, button.getItemStack(player)));

        player.openInventory(inventory);
        return inventory;
    }

    private void updateSlot(Player player, int slot) {
        final Inventory inventory = viewers.get(player.getUniqueId());
        if (inventory == null)
            return;

        final Button button = buttons.get(slot);
        if (button != null) {
            inventory.setItem(slot, button.getItemStack(player));
        }
    }

    private List<String> getDisabledWorlds() {
        return Files.CONFIG.getFile().getStringList("Settings.DisabledWorlds");
    }


}
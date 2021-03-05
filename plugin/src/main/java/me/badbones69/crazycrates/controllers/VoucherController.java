package me.badbones69.crazycrates.controllers;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.badbones69.crazycrates.api.CrazyCrates;
import me.badbones69.crazycrates.api.enums.KeyType;
import me.badbones69.crazycrates.api.enums.Messages;
import me.badbones69.crazycrates.api.objects.Crate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class VoucherController implements Listener {

    private static final String NBT_TAG = "cc_voucher";

    private final CrazyCrates plugin;

    public VoucherController(CrazyCrates plugin) {
        this.plugin = plugin;
    }

    public void giveVoucher(Player player, Crate crate, int amount) {
        final ItemStack voucher = plugin.getVoucher(crate.getFile());
        if (voucher == null) {
            player.sendMessage(ChatColor.RED + "No voucher for " + crate.getName());
            return;
        }

        final NBTItem item = new NBTItem(voucher);
        item.setString(NBT_TAG, crate.getName());
        item.applyNBT(voucher);

        voucher.setAmount(amount);

        player.getInventory().addItem(voucher);
    }

    public boolean redeemVoucher(Player player, boolean takeItem) {
        final ItemStack mainHand = plugin.getNMSSupport().getItemInMainHand(player);
        if (mainHand == null || mainHand.getType() == Material.AIR)
            return false;

        NBTItem item = new NBTItem(mainHand);
        if (!item.hasKey(NBT_TAG))
            return false;

        final Crate crate = plugin.getCrateFromName(item.getString(NBT_TAG));
        if (crate == null) {
            player.sendMessage(Messages.NOT_A_CRATE.getMessage("%crate%", item.getString(NBT_TAG)));
            return false;
        }

        int takeAmount = player.isSneaking()
                ? mainHand.getAmount()
                : 1;

        plugin.addKeys(takeAmount, player, crate, KeyType.VIRTUAL_KEY);
        player.sendMessage(Messages.PLAYER_REDEEM_VOUCHER.getMessage(
                Map.of("%crate%", crate.getName(), "%amount%", String.valueOf(takeAmount))));

        player.playSound(player.getLocation(), plugin.getSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP"), 1, 1);

        if (takeItem) {
            // remove
            if (mainHand.getAmount() - takeAmount > 1) {
                mainHand.setAmount(mainHand.getAmount() - takeAmount);
                player.setItemInHand(mainHand);
            } else {
                player.setItemInHand(null);
            }
        }

        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (redeemVoucher(event.getPlayer(), true)) {
                event.setCancelled(true);
            }
        }
    }

}

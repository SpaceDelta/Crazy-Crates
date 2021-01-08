package me.badbones69.crazycrates.controllers.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface Button {

    /**
     * Method called when the inventory is opened
     *
     * @param viewer the person viewing the stack
     * @return the item stack to show to the player
     */
    ItemStack getItemStack(Player viewer);

    /**
     * Method called when the button is interacted with
     *
     * @param clicker   the player who clicked it
     * @param clickType how they clicked it
     */
    void onAction(Player clicker, ClickType clickType);

}

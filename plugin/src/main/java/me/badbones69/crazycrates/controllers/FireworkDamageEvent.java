package me.badbones69.crazycrates.controllers;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class FireworkDamageEvent implements Listener {

    private static ArrayList<Entity> fireworks = new ArrayList<>();
    private Plugin plugin;

    public FireworkDamageEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @return All the active fireworks.
     */
    public static ArrayList<Entity> getFireworks() {
        return fireworks;
    }

    /**
     * @param firework The firework you want to add.
     */
    public static void addFirework(Entity firework) {
        fireworks.add(firework);
    }

    /**
     * @param firework The firework you are removing.
     */
    public static void removeFirework(Entity firework) {
        fireworks.remove(firework);
    }

    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent e) {
        if (fireworks.contains(e.getDamager())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFireworkExplode(FireworkExplodeEvent e) {
        final Entity firework = e.getEntity();
        if (getFireworks().contains(firework)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    fireworks.remove(firework);
                }
            }.runTaskLater(plugin, 5);
        }
    }

}
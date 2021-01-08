package me.badbones69.crazycrates.api.objects;

import org.bukkit.inventory.ItemStack;

public class Tier {

    private String name;
    private String coloredName;
    private ItemBuilder colorGlass;
    private Integer chance;
    private Integer maxRange;

    public Tier(String name, String coloredName, ItemBuilder colorGlass, Integer chance, Integer maxRange) {
        this.name = name;
        this.coloredName = coloredName;
        this.colorGlass = colorGlass.setName(coloredName);
        this.chance = chance;
        this.maxRange = maxRange;
    }

    public Tier(String name, String coloredName, String colorGlass, Integer chance, Integer maxRange) {
        this.name = name;
        this.coloredName = coloredName;
        this.colorGlass = new ItemBuilder().setMaterial(colorGlass).setName(coloredName);
        this.chance = chance;
        this.maxRange = maxRange;
    }

    /**
     * @return Name of the tier.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The colored name of the tier.
     */
    public String getColoredName() {
        return coloredName;
    }

    /**
     * @return The colored glass pane.
     */
    public ItemBuilder getColorGlass() {
        return colorGlass;
    }

    /**
     * @return The chance of being picked.
     */
    public Integer getChance() {
        return chance;
    }

    /**
     * @return The range of max possible\ chances.
     */
    public Integer getMaxRange() {
        return maxRange;
    }

    public ItemStack getTierPane() {
        return colorGlass.build();
    }

}

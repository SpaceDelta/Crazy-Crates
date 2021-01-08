package me.badbones69.crazycrates.api.enums;

public enum CrateType {

    MENU("Menu"),
    COSMIC("Cosmic"),
    CRATE_ON_THE_GO("CrateOnTheGo"),
    // Start SpaceDelta
    VIRTUAL_ON_THE_GO("VirtualOnTheGo"),
    // End SpaceDelta
    CSGO("CSGO"),
    FIRE_CRACKER("FireCracker"),
    QUAD_CRATE("QuadCrate"),
    QUICK_CRATE("QuickCrate"),
    ROULETTE("Roulette"),
    WHEEL("Wheel"),
    WONDER("Wonder"),
    WAR("War");

    private final String name;

    CrateType(String name) {
        this.name = name;
    }

    public static CrateType getFromName(String name) {
        for (CrateType crate : CrateType.values()) {
            if (crate.getName().equalsIgnoreCase(name)) {
                return crate;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

}
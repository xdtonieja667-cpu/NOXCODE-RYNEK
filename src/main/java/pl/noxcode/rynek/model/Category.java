package pl.noxcode.rynek.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Category {
    ZBROJE("Zbroje"),
    NARZEDZIA("Narzędzia"),
    INNE("Inne");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return INNE;
        }

        Material type = item.getType();
        String name = type.name();

        if (name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS")
                || type == Material.ELYTRA
                || type == Material.TURTLE_HELMET
                || type == Material.SHIELD) {
            return ZBROJE;
        }

        if (name.endsWith("_SWORD")
                || name.endsWith("_AXE")
                || name.endsWith("_PICKAXE")
                || name.endsWith("_SHOVEL")
                || name.endsWith("_HOE")
                || type == Material.BOW
                || type == Material.CROSSBOW
                || type == Material.TRIDENT
                || type == Material.FISHING_ROD
                || type == Material.SHEARS
                || type == Material.FLINT_AND_STEEL
                || type == Material.MACE
                || type == Material.WIND_CHARGE) {
            return NARZEDZIA;
        }

        return INNE;
    }
}

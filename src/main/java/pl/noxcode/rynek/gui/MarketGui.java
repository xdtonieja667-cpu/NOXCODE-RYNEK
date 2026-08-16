package pl.noxcode.rynek.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.noxcode.rynek.RynekPlugin;
import pl.noxcode.rynek.model.Category;
import pl.noxcode.rynek.model.MarketListing;
import pl.noxcode.rynek.service.EconomyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MarketGui {

    public static final int CATEGORY_SIZE = 27;
    public static final int LISTING_SIZE = 54;

    private MarketGui() {
    }

    public static void openCategoryMenu(RynekPlugin plugin, Player player) {
        CategoryMenuHolder holder = new CategoryMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, CATEGORY_SIZE, Component.text("NOXCODE - Rynek"));
        holder.setInventory(inventory);

        inventory.setItem(11, createCategoryItem(
                Material.IRON_CHESTPLATE,
                Category.ZBROJE,
                plugin.getMarketManager().getListingsByCategory(Category.ZBROJE).size()
        ));
        inventory.setItem(13, createCategoryItem(
                Material.IRON_PICKAXE,
                Category.NARZEDZIA,
                plugin.getMarketManager().getListingsByCategory(Category.NARZEDZIA).size()
        ));
        inventory.setItem(15, createCategoryItem(
                Material.CHEST,
                Category.INNE,
                plugin.getMarketManager().getListingsByCategory(Category.INNE).size()
        ));

        fillBorder(inventory, Material.GRAY_STAINED_GLASS_PANE);
        player.openInventory(inventory);
    }

    public static void openListingMenu(RynekPlugin plugin, Player player, Category category) {
        List<MarketListing> listings = plugin.getMarketManager().getListingsByCategory(category);
        ListingMenuHolder holder = new ListingMenuHolder(category);
        Inventory inventory = Bukkit.createInventory(
                holder,
                LISTING_SIZE,
                Component.text("NOXCODE - " + category.getDisplayName())
        );
        holder.setInventory(inventory);

        EconomyService economy = plugin.getEconomyService();
        int slot = 0;
        for (MarketListing listing : listings) {
            if (slot >= LISTING_SIZE - 9) {
                break;
            }

            ItemStack display = listing.createDisplayItem();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("Sprzedawca: ", NamedTextColor.GRAY)
                        .append(Component.text(listing.getSellerName(), NamedTextColor.YELLOW)));
                lore.add(Component.text("Ilosc: ", NamedTextColor.GRAY)
                        .append(Component.text(listing.getAmount(), NamedTextColor.WHITE)));
                lore.add(Component.text("Cena: ", NamedTextColor.GRAY)
                        .append(Component.text(economy.format(listing.getPrice()), NamedTextColor.GREEN)));
                lore.add(Component.empty());
                lore.add(Component.text("Kliknij, aby kupic", NamedTextColor.AQUA));
                meta.lore(lore);
                display.setItemMeta(meta);
            }

            inventory.setItem(slot++, display);
            holder.registerSlot(slot - 1, listing.getId());
        }

        inventory.setItem(LISTING_SIZE - 5, createBackButton());
        fillRemaining(inventory, Material.BLACK_STAINED_GLASS_PANE);
        player.openInventory(inventory);
    }

    private static ItemStack createCategoryItem(Material material, Category category, int count) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(category.getDisplayName(), NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Oferty: " + count, NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.empty(),
                    Component.text("Kliknij, aby otworzyc", NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Powrot", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void fillBorder(Inventory inventory, Material material) {
        ItemStack filler = createFiller(material);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private static void fillRemaining(Inventory inventory, Material material) {
        ItemStack filler = createFiller(material);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private static ItemStack createFiller(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static class CategoryMenuHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static class ListingMenuHolder implements InventoryHolder {
        private final Category category;
        private final Map<Integer, String> slotToListingId = new HashMap<>();
        private Inventory inventory;

        public ListingMenuHolder(Category category) {
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }

        public void registerSlot(int slot, String listingId) {
            slotToListingId.put(slot, listingId);
        }

        public String getListingId(int slot) {
            return slotToListingId.get(slot);
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }
}

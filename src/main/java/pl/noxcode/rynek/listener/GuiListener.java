package pl.noxcode.rynek.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.noxcode.rynek.RynekPlugin;
import pl.noxcode.rynek.gui.MarketGui;
import pl.noxcode.rynek.model.Category;
import pl.noxcode.rynek.model.MarketListing;
import pl.noxcode.rynek.service.EconomyService;
import pl.noxcode.rynek.util.Messages;

import java.util.HashMap;

public class GuiListener implements Listener {

    private final RynekPlugin plugin;

    public GuiListener(RynekPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MarketGui.CategoryMenuHolder)
                && !(holder instanceof MarketGui.ListingMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) {
            return;
        }

        if (holder instanceof MarketGui.CategoryMenuHolder) {
            handleCategoryClick(player, clicked);
            return;
        }

        handleListingClick(player, (MarketGui.ListingMenuHolder) holder, event.getSlot(), clicked);
    }

    private void handleCategoryClick(Player player, ItemStack clicked) {
        Category category = switch (clicked.getType()) {
            case IRON_CHESTPLATE -> Category.ZBROJE;
            case IRON_PICKAXE -> Category.NARZEDZIA;
            case CHEST -> Category.INNE;
            default -> null;
        };

        if (category != null) {
            MarketGui.openListingMenu(plugin, player, category);
        }
    }

    private void handleListingClick(
            Player player,
            MarketGui.ListingMenuHolder holder,
            int slot,
            ItemStack clicked
    ) {
        if (clicked.getType() == org.bukkit.Material.ARROW && slot == MarketGui.LISTING_SIZE - 5) {
            MarketGui.openCategoryMenu(plugin, player);
            return;
        }

        String listingId = holder.getListingId(slot);
        if (listingId == null) {
            return;
        }

        MarketListing listing = plugin.getMarketManager().getListing(listingId);
        if (listing == null) {
            Messages.send(player, "&cTa oferta juz nie istnieje.");
            MarketGui.openListingMenu(plugin, player, holder.getCategory());
            return;
        }

        if (listing.getSellerId().equals(player.getUniqueId())) {
            Messages.send(player, "&cNie mozesz kupic wlasnej oferty.");
            return;
        }

        EconomyService economy = plugin.getEconomyService();
        double price = listing.getPrice();

        if (!economy.has(player, price)) {
            Messages.send(player, "&cNie masz wystarczajaco pieniedzy. Potrzebujesz: " + economy.format(price));
            return;
        }

        ItemStack purchasedItem = listing.getItem();
        purchasedItem.setAmount(listing.getAmount());
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(purchasedItem.clone());
        if (!leftover.isEmpty()) {
            Messages.send(player, "&cNie masz miejsca w ekwipunku.");
            return;
        }

        if (!economy.withdraw(player, price)) {
            player.getInventory().removeItem(purchasedItem.clone());
            Messages.send(player, "&cNie udalo sie pobrac pieniedzy. Zakup anulowany.");
            return;
        }

        plugin.getMarketManager().removeListing(listingId);

        Player seller = plugin.getServer().getPlayer(listing.getSellerId());
        if (seller != null && seller.isOnline()) {
            economy.deposit(seller, price);
            Messages.send(seller, "&aSprzedales &f" + listing.getAmount() + "x &aprzedmiot graczowi &f" +
                    player.getName() + " &aza &f" + economy.format(price) + "&a.");
        } else {
            economy.depositOffline(listing.getSellerName(), price);
        }

        Messages.send(player, "&aKupiles przedmiot od &f" + listing.getSellerName() +
                " &aza &f" + economy.format(price) + "&a.");
        MarketGui.openListingMenu(plugin, player, holder.getCategory());
    }
}

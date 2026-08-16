package pl.noxcode.rynek.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import pl.noxcode.rynek.RynekPlugin;
import pl.noxcode.rynek.model.Category;
import pl.noxcode.rynek.model.MarketListing;
import pl.noxcode.rynek.util.ItemUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MarketManager {

    private final RynekPlugin plugin;
    private final File listingsFile;
    private final Map<String, MarketListing> listings = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public MarketManager(RynekPlugin plugin) {
        this.plugin = plugin;
        this.listingsFile = new File(plugin.getDataFolder(), "listings.yml");
    }

    public void load() {
        listings.clear();
        idCounter.set(1);

        if (!listingsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(listingsFile);
        ConfigurationSection section = config.getConfigurationSection("listings");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }

            try {
                MarketListing listing = deserialize(key, entry);
                listings.put(key, listing);

                try {
                    int numericId = Integer.parseInt(key.replace("listing-", ""));
                    idCounter.updateAndGet(current -> Math.max(current, numericId + 1));
                } catch (NumberFormatException ignored) {
                    // Keep default counter for non-standard ids.
                }
            } catch (Exception exception) {
                plugin.getLogger().warning("Nie udalo sie wczytac oferty " + key + ": " + exception.getMessage());
            }
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection section = config.createSection("listings");

        for (Map.Entry<String, MarketListing> entry : listings.entrySet()) {
            serialize(section.createSection(entry.getKey()), entry.getValue());
        }

        try {
            config.save(listingsFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Nie udalo sie zapisac ofert: " + exception.getMessage());
        }
    }

    public MarketListing addListing(
            UUID sellerId,
            String sellerName,
            ItemStack item,
            int amount,
            double price,
            Category category
    ) {
        String id = "listing-" + idCounter.getAndIncrement();
        MarketListing listing = new MarketListing(
                id,
                sellerId,
                sellerName,
                item,
                amount,
                price,
                category,
                System.currentTimeMillis()
        );
        listings.put(id, listing);
        save();
        return listing;
    }

    public MarketListing removeListing(String id) {
        MarketListing removed = listings.remove(id);
        if (removed != null) {
            save();
        }
        return removed;
    }

    public MarketListing getListing(String id) {
        return listings.get(id);
    }

    public Collection<MarketListing> getAllListings() {
        return Collections.unmodifiableCollection(listings.values());
    }

    public List<MarketListing> getListingsByCategory(Category category) {
        return listings.values().stream()
                .filter(listing -> listing.getCategory() == category)
                .sorted((a, b) -> Long.compare(a.getCreatedAt(), b.getCreatedAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void serialize(ConfigurationSection section, MarketListing listing) {
        section.set("seller-id", listing.getSellerId().toString());
        section.set("seller-name", listing.getSellerName());
        section.set("item", ItemUtil.serialize(listing.getItem()));
        section.set("amount", listing.getAmount());
        section.set("price", listing.getPrice());
        section.set("category", listing.getCategory().name());
        section.set("created-at", listing.getCreatedAt());
    }

    private MarketListing deserialize(String id, ConfigurationSection section) {
        UUID sellerId = UUID.fromString(section.getString("seller-id"));
        String sellerName = section.getString("seller-name");
        ItemStack item = ItemUtil.deserialize(section.getString("item"));
        int amount = section.getInt("amount");
        double price = section.getDouble("price");
        Category category = Category.valueOf(section.getString("category"));
        long createdAt = section.getLong("created-at");

        return new MarketListing(id, sellerId, sellerName, item, amount, price, category, createdAt);
    }
}

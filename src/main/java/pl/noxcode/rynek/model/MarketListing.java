package pl.noxcode.rynek.model;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MarketListing {

    private final String id;
    private final UUID sellerId;
    private final String sellerName;
    private final ItemStack item;
    private final int amount;
    private final double price;
    private final Category category;
    private final long createdAt;

    public MarketListing(
            String id,
            UUID sellerId,
            String sellerName,
            ItemStack item,
            int amount,
            double price,
            Category category,
            long createdAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.item = item.clone();
        this.amount = amount;
        this.price = price;
        this.category = category;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public int getAmount() {
        return amount;
    }

    public double getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public ItemStack createDisplayItem() {
        ItemStack display = item.clone();
        display.setAmount(Math.min(amount, display.getMaxStackSize()));
        return display;
    }
}

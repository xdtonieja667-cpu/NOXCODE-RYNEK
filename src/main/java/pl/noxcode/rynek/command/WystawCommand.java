package pl.noxcode.rynek.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.noxcode.rynek.RynekPlugin;
import pl.noxcode.rynek.model.Category;
import pl.noxcode.rynek.service.EconomyService;
import pl.noxcode.rynek.util.Messages;

public class WystawCommand implements CommandExecutor {

    private final RynekPlugin plugin;

    public WystawCommand(RynekPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "&cTa komenda jest dostepna tylko dla graczy.");
            return true;
        }

        if (!player.hasPermission("rynek.sell")) {
            Messages.send(player, "&cNie masz uprawnien do wystawiania przedmiotow.");
            return true;
        }

        if (args.length != 2) {
            Messages.send(player, "&eUzycie: /wystaw <ilosc> <cena>");
            return true;
        }

        int amount;
        double price;
        try {
            amount = Integer.parseInt(args[0]);
            price = Double.parseDouble(args[1].replace(",", "."));
        } catch (NumberFormatException exception) {
            Messages.send(player, "&cPodaj poprawna ilosc (liczba calkowita) i cene (liczba).");
            return true;
        }

        if (amount <= 0) {
            Messages.send(player, "&cIlosc musi byc wieksza od zera.");
            return true;
        }

        if (price <= 0) {
            Messages.send(player, "&cCena musi byc wieksza od zera.");
            return true;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType().isAir()) {
            Messages.send(player, "&cMusisz trzymac przedmiot w rece, aby go wystawic.");
            return true;
        }

        if (handItem.getAmount() < amount) {
            Messages.send(player, "&cMasz za malo tego przedmiotu w rece. Trzymasz: " + handItem.getAmount());
            return true;
        }

        ItemStack listingItem = handItem.clone();
        listingItem.setAmount(amount);

        handItem.setAmount(handItem.getAmount() - amount);
        if (handItem.getAmount() <= 0) {
            player.getInventory().setItemInMainHand(null);
        }

        Category category = Category.fromItem(listingItem);
        plugin.getMarketManager().addListing(
                player.getUniqueId(),
                player.getName(),
                listingItem,
                amount,
                price,
                category
        );

        EconomyService economy = plugin.getEconomyService();
        Messages.send(player, "&aWystawiles &f" + amount + "x " + formatMaterial(listingItem) +
                " &aw kategorii &f" + category.getDisplayName() +
                " &aza &f" + economy.format(price) + "&a.");
        return true;
    }

    private String formatMaterial(ItemStack item) {
        String name = item.getType().name().toLowerCase().replace('_', ' ');
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}

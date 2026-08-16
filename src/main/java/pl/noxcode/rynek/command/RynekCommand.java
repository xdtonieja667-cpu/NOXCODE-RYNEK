package pl.noxcode.rynek.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.noxcode.rynek.RynekPlugin;
import pl.noxcode.rynek.gui.MarketGui;
import pl.noxcode.rynek.util.Messages;

public class RynekCommand implements CommandExecutor {

    private final RynekPlugin plugin;

    public RynekCommand(RynekPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Messages.send(sender, "&cTa komenda jest dostepna tylko dla graczy.");
            return true;
        }

        if (!player.hasPermission("rynek.use")) {
            Messages.send(player, "&cNie masz uprawnien do korzystania z rynku.");
            return true;
        }

        MarketGui.openCategoryMenu(plugin, player);
        return true;
    }
}

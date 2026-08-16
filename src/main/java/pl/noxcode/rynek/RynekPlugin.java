package pl.noxcode.rynek;

import org.bukkit.plugin.java.JavaPlugin;
import pl.noxcode.rynek.command.RynekCommand;
import pl.noxcode.rynek.command.WystawCommand;
import pl.noxcode.rynek.listener.GuiListener;
import pl.noxcode.rynek.service.EconomyService;
import pl.noxcode.rynek.service.MarketManager;

public final class RynekPlugin extends JavaPlugin {

    private EconomyService economyService;
    private MarketManager marketManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        economyService = new EconomyService(this);
        if (!economyService.setup()) {
            getLogger().severe("Nie znaleziono providera ekonomii Vault! Wylaczam plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        marketManager = new MarketManager(this);
        marketManager.load();

        getCommand("rynek").setExecutor(new RynekCommand(this));
        getCommand("wystaw").setExecutor(new WystawCommand(this));
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("NOXCODE-RYNEK zostal wlaczony.");
    }

    @Override
    public void onDisable() {
        if (marketManager != null) {
            marketManager.save();
        }
        getLogger().info("NOXCODE-RYNEK zostal wylaczony.");
    }

    public EconomyService getEconomyService() {
        return economyService;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }
}

package me.zachary.sellwand;

import me.zachary.sellwand.commands.GiveCommand;
import me.zachary.sellwand.commands.ReloadCommand;
import me.zachary.sellwand.listeners.PlayerInteractListener;
import me.zachary.sellwand.wands.SellWandManager;
import me.zachary.zachcore.ZachCorePlugin;
import me.zachary.zachcore.config.Config;
import me.zachary.zachcore.utils.Metrics;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.hooks.HologramManager;
import me.zachary.zachcore.utils.hooks.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

public final class Sellwand extends ZachCorePlugin {

	private static Sellwand instance;
	private SellWandManager sellWandManager;
	private final Config sellWandConfig = new Config();

	@Override
	public void onEnable() {
		instance = this;
		preEnable(this);

		int pluginId = 9724;
		Metrics metrics = new Metrics(this, pluginId);
		EconomyManager.load();
		HologramManager.load(this);
		//Updatechecker.update(this, "sellwand");

		reload();

		// Load listeners
		new PlayerInteractListener(this);

		// Load Commands
		new GiveCommand(this);
		new ReloadCommand(this);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	public void reload(){
		saveDefaultConfig();
		reloadConfig();

		File sellWandConfigFile = new File(this.getDataFolder(), "sellwand.yml");

		if (!sellWandConfigFile.exists())
			saveResource("sellwand.yml", false);

		try {
			sellWandConfig.load(sellWandConfigFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		sellWandManager = new SellWandManager(this);
		this.setLocale(getConfig().getString("system.locale"), true);
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> ShopManager.load(this), 60L);
	}

	public static Sellwand getInstance() {
		return instance;
	}

	public SellWandManager getSellWandManager() {
		return sellWandManager;
	}

	public Config getSellWandConfig() {
		return sellWandConfig;
	}

	@Override
	public void onDataLoad() {

	}
}

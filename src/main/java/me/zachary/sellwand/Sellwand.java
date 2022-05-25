package me.zachary.sellwand;

import me.zachary.sellwand.commands.GiveCommand;
import me.zachary.sellwand.commands.ReloadCommand;
import me.zachary.sellwand.listeners.PlayerInteractListener;
import me.zachary.sellwand.wands.SellWandBuilder;
import me.zachary.zachcore.ZachCorePlugin;
import me.zachary.zachcore.utils.Metrics;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.hooks.HologramManager;
import me.zachary.zachcore.utils.hooks.ShopManager;
import org.bukkit.Bukkit;

public final class Sellwand extends ZachCorePlugin {

	private static Sellwand instance;

	@Override
	public void onEnable() {
		instance = this;
		preEnable(this);

		int pluginId = 9724;
		Metrics metrics = new Metrics(this, pluginId);
		EconomyManager.load();
		HologramManager.load(this);
		//Updatechecker.update(this, "sellwand");
		saveDefaultConfig();

		// Load Message file.
		this.setLocale(getConfig().getString("system.locale"), true);

		// Load listeners
		new PlayerInteractListener(this);

		// Load Commands
		new GiveCommand(this);
		new ReloadCommand(this);

		Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> ShopManager.load(this), 60L);
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	public static Sellwand getInstance() {
		return instance;
	}

	public SellWandBuilder getSellWandBuilder() {
		return new SellWandBuilder(this);
	}

	@Override
	public void onDataLoad() {

	}
}

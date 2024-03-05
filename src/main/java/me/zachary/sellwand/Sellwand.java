package me.zachary.sellwand;

import me.zachary.sellwand.commands.GiveCommand;
import me.zachary.sellwand.commands.ReloadCommand;
import me.zachary.sellwand.listeners.AnvilListener;
import me.zachary.sellwand.listeners.EnchantListener;
import me.zachary.sellwand.listeners.GrindstoneListener;
import me.zachary.sellwand.listeners.PlayerInteractListener;
import me.zachary.sellwand.wands.SellWandManager;
import me.zachary.zachcore.ZachCorePlugin;
import me.zachary.zachcore.config.Config;
import me.zachary.zachcore.utils.Metrics;
import me.zachary.zachcore.utils.ReflectionUtils;
import me.zachary.zachcore.utils.ServerVersion;
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
		if(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14))
			new GrindstoneListener(this);
		new EnchantListener(this);
		new AnvilListener(this);

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

		if(getConfig().isSet("Sell wand")){
			sellWandConfig.set("sellwand.old.name", getConfig().getString("Sell wand.Name"));
			sellWandConfig.set("sellwand.old.material", getConfig().getString("Sell wand.Item"));
			sellWandConfig.set("sellwand.old.glowing", getConfig().getBoolean("Sell wand.Glowing"));
			sellWandConfig.set("sellwand.old.multiplier", 1.0D);
			sellWandConfig.set("sellwand.old.uses", 100);
			sellWandConfig.set("sellwand.old.lore", getConfig().getStringList("Sell wand.Lore"));
			try {
				sellWandConfig.save(sellWandConfigFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			getConfig().set("Sell wand", null);
			saveConfig();
			reloadConfig();
		}

		sellWandManager = new SellWandManager(this);
		this.setLocale(getConfig().getString("system.locale"), true);
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
			ShopManager.load(this);
			EconomyManager.load();
		}, 60L);
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

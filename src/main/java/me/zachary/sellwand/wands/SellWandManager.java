package me.zachary.sellwand.wands;

import me.zachary.sellwand.Sellwand;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class SellWandManager {
	private Sellwand plugin;
	private List<OSellwand> sellwands;
	
	public SellWandManager(Sellwand plugin) {
		this.plugin = plugin;
		this.sellwands = new ArrayList<>();
		loadSellWand();
	}

	private void loadSellWand() {
		for(String id : plugin.getSellWandConfig().getConfigurationSection("sellwand").getKeys(false)){
			ConfigurationSection section = plugin.getSellWandConfig().getConfigurationSection("sellwand." + id);
			if(section != null)
				sellwands.add(new OSellwand(
						id,
						section.getString("name"),
						section.getBoolean("glowing"),
						section.getString("material"),
						section.getStringList("lore"),
						section.getDouble("multiplier"),
						section.getInt("uses")
				));
		}
	}

	public List<OSellwand> getSellwands() {
		return sellwands;
	}

	public OSellwand getSellwand(String id) {
		for(OSellwand sellwand : sellwands) {
			if(sellwand.getId().equals(id))
				return sellwand;
		}
		return null;
	}
}

package me.zachary.sellwand.wands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.zachcore.utils.items.ItemBuilder;
import me.zachary.zachcore.utils.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OSellwand {
	private String id, name;

	private Boolean glowing;

	private XMaterial material;

	private List<String> lore;
	private Double multiplier;
	private Integer uses;

	public OSellwand(String id, String name, Boolean glowing, String material, List<String> lore, Double multiplier, int uses) {
		this.id = id;
		this.name = name;
		this.glowing = glowing;
		try {
			this.material = XMaterial.valueOf(material.toUpperCase());
		} catch (Exception e) {
			Bukkit.getLogger().info("[Sellwand] The material " + material + " is not found in sellwand id " + id);
			this.material = XMaterial.STICK;
		}
		this.lore = lore;
		this.multiplier = multiplier;
		this.uses = uses;
	}

	public String getId() {
		return id;
	}

	public Double getMultiplier() {
		return multiplier;
	}

	public int getUses() {
		return uses;
	}

	public ItemStack getSellWand(){
		return getSellWand(null);
	}

	public ItemStack getSellWand(Integer uses) {
		ItemBuilder sellWand = new ItemBuilder(material.parseItem())
				.name(name.replace("%multiplier%", String.valueOf(multiplier))
						.replace("%uses%", String.valueOf(uses == null ? this.uses : uses)))
				.lore(getLore(uses));
		if (glowing)
			sellWand.enchant(Enchantment.ARROW_INFINITE, 1).flag(ItemFlag.HIDE_ENCHANTS);
		NBTItem sellWandNBT = new NBTItem(sellWand.build());
		sellWandNBT.setString("id", id);
		sellWandNBT.setDouble("Multiplier", multiplier);
		sellWandNBT.setInteger("Uses", (uses == null ? this.uses : uses));
		sellWandNBT.setObject("UUID_Sellwand", UUID.randomUUID());
		return sellWandNBT.getItem();
	}

	private List<String> getLore(Integer uses) {
		List<String> lore = new ArrayList<>();
		for (String line : this.lore) {
			lore.add(line.replace("%multiplier%", String.valueOf(multiplier))
					.replace("%uses%", String.valueOf(uses == null ? this.uses : uses)));
		}
		return lore;
	}
}

package me.zachary.sellwand.listeners;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.objects.ChestType;
import com.bgsoftware.wildchests.api.objects.chests.StorageChest;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.utils.*;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.hooks.HologramManager;
import me.zachary.zachcore.utils.hooks.ShopManager;
import me.zachary.zachcore.utils.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerInteractListener implements Listener {
	private final Sellwand plugin;
	private CompatibleSound sellSound = null;
	private CompatibleSound errorSound = null;

	public PlayerInteractListener(Sellwand plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);

		try {
			sellSound = CompatibleSound.valueOf(plugin.getConfig().isSet("Sound.Sell") ? plugin.getConfig().getString("Sound.Sell") : null);
			errorSound = CompatibleSound.valueOf(plugin.getConfig().isSet("Sound.Error") ? plugin.getConfig().getString("Sound.Error") : null);
		} catch (Exception e) {
			plugin.getLogger().warning("Sounds are not valid in config.yml!");
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled() ||
				!(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) ||
				(!ReflectionUtils.getVersion().contains("1_8") && event.getHand() == EquipmentSlot.OFF_HAND) ||
				event.getItem() == null)
			return;

		/*
		 * Action.LEFT_CLICK_BLOCK = HOLOGRAM
		 * Action.RIGHT_CLICK_BLOCK = SELL
		 */

		NBTItem item = new NBTItem(event.getItem());
		if (!(item.hasKey("Is a sell wand") || item.hasKey("UUID_Sellwand")))
			return;
		event.getItem().setDurability((short) 0);

		if (event.getClickedBlock() == null)
			return;

		Player player = event.getPlayer();

		if ((event.getAction() == Action.LEFT_CLICK_BLOCK && !player.hasPermission("sellwand.hologram")) ||
				(event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.hasPermission("sellwand.use"))) {
			if(errorSound != null)
				errorSound.play(player);
			plugin.getLocale().getMessage("command.no-permission").sendPrefixedMessage(player);
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && CooldownBuilder.isCooldown("Use cooldown", player.getUniqueId())) {
			event.setCancelled(true);
			plugin.getLocale().getMessage("sellwand.cooldown")
					.processPlaceholder("seconds", CooldownBuilder.getCooldown("Use cooldown", player.getUniqueId()) / 1000)
					.sendPrefixedMessage(player);
			return;
		}

		double amount = 0D;
		int itemAmount = 0;

		if(Bukkit.getPluginManager().isPluginEnabled("WildChests") && WildChestsAPI.getChest(event.getClickedBlock().getLocation()) instanceof StorageChest){
			com.bgsoftware.wildchests.api.objects.chests.Chest chest = WildChestsAPI.getChest(event.getClickedBlock().getLocation());
			if(chest != null && chest.getChestType().equals(ChestType.STORAGE_UNIT)) {
				event.setCancelled(true);
				StorageChest storageChest = (StorageChest) chest;
				ItemStack itemStack = storageChest.getItemStack();

				double price = ShopManager.getSellPrice(player, itemStack, Integer.parseInt(String.valueOf(storageChest.getAmount())));

				if(price >= 0D) {
					amount += price;
					itemAmount += Integer.parseInt(String.valueOf(storageChest.getAmount()));
					if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
						storageChest.setAmount(0);
						storageChest.update();
					}
				}
			}
		}else {
			Inventory contents = StorageUtils.getStorageContents(event.getClickedBlock());
			if (contents == null) {
				return;
			}
			event.setCancelled(true);

			for (int i = 0; i < contents.getContents().length; i++) {
				ItemStack chestItem = contents.getItem(i);
				double price = 0D;
				if (chestItem != null) {
					price = ShopManager.getSellPrice(player, chestItem, chestItem.getAmount());
				}
				if (price >= 0 && chestItem != null) {
					if(event.getAction() == Action.RIGHT_CLICK_BLOCK) contents.setItem(i, XMaterial.AIR.parseItem());
					itemAmount += chestItem.getAmount();
					amount += price;
				}
			}
		}
		double multiplier = item.getDouble("Multiplier");
		amount = amount * multiplier;

		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// Hologram
			Location hologramLoc = null;
			if (!ServerVersion.isServerVersionAtOrBelow(ServerVersion.V1_12))
				hologramLoc = event.getClickedBlock().getLocation().add(getDifferenceX(player), -0.80, getDifferenceZ(player));
			else
				hologramLoc = event.getClickedBlock().getLocation();
			hologramLoc.setDirection(player.getLocation().getDirection());
			HologramManager.removeHologram(hologramLoc);
			HologramManager.createHologram(hologramLoc, getHologramLine(itemAmount, amount));
			Location finalHologramLoc = hologramLoc;
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					HologramManager.removeHologram(finalHologramLoc);
				}
			}, (plugin.getConfig().getInt("Hologram time", 5) * 20L));
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// Sell
			int uses = item.getInteger("Uses");
			if (uses == 0) {
				plugin.getLocale().getMessage("sellwand.no-uses").sendPrefixedMessage(player);
				if(errorSound != null)
					errorSound.play(player);
				return;
			}

			if (plugin.getConfig().getBoolean("Log sell with sell wand in console") && amount != 0D)
				plugin.getLog().log("Player " + player.getName() + " sell " + itemAmount + " items for a total of " + EconomyManager.formatEconomy(amount));

			if (amount != 0D) {
				EconomyManager.deposit(player, amount);
				if (uses != -1) {
					--uses;
					if (plugin.getConfig().getBoolean("Destroy wand") && uses == 0)
						event.getItem().setAmount(0);
					else
						PlayerInventoryUtils.setInMainHand(player, plugin.getSellWandManager().getSellwand((item.getString("id").isEmpty() ? "old" : item.getString("id"))).getSellWand(uses));
				}
				plugin.getLocale().getMessage("sellwand.sell-success")
						.processPlaceholder("price", EconomyManager.formatEconomy(amount))
						.processPlaceholder("item_amount", String.valueOf(itemAmount))
						.sendPrefixedMessage(player);
				int cooldown = PermissionUtils.getNumberFromPermission(player, "sellwand.cooldown", false, 0);
				CooldownBuilder.addCooldown("Use cooldown", player.getUniqueId(), cooldown);
				if(sellSound != null)
					sellSound.play(player);
			} else {
				if(errorSound != null)
					errorSound.play(player);
				plugin.getLocale().getMessage("sellwand.sell-nothing").sendPrefixedMessage(player);
			}
		}
	}

	public List<String> getHologramLine(int amount, double price) {
		List<String> line = new ArrayList<>();
		for (String i : Sellwand.getInstance().getConfig().getStringList("Hologram line")) {
			line.add(i.replace("%amount%", String.valueOf(amount))
					.replace("%price%", EconomyManager.formatEconomy(price)));
		}
		return line;
	}

	private double getDifferenceX(Player player) {
		double direction = 0;
		switch (player.getFacing().toString()) {
			case "WEST":
				direction = 1;
				break;
			case "EAST":
				direction = -1;
				break;
			case "SOUTH":
			case "NORTH":
				direction = 0;
				break;
			default:
				break;
		}
		return direction;
	}

	private double getDifferenceZ(Player player) {
		double direction = 0;
		switch (player.getFacing().toString()) {
			case "WEST":
			case "EAST":
				direction = 0;
				break;
			case "SOUTH":
				direction = -1;
				break;
			case "NORTH":
				direction = 1;
				break;
			default:
				break;
		}
		return direction;
	}
}

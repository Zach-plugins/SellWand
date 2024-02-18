package me.zachary.sellwand.listeners;

import com.bgsoftware.wildchests.api.WildChestsAPI;
import com.bgsoftware.wildchests.api.objects.ChestType;
import com.bgsoftware.wildchests.api.objects.chests.StorageChest;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import me.zachary.sellwand.api.events.SellwandHologramEvent;
import me.zachary.sellwand.api.events.SellwandSellEvent;
import me.zachary.zachcore.utils.*;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.hooks.HologramManager;
import me.zachary.zachcore.utils.hooks.ShopManager;
import me.zachary.zachcore.utils.xseries.XMaterial;
import net.bestemor.superhoppers.SuperHoppersAPI;
import net.bestemor.superhoppers.hopper.SuperHopper;
import net.bestemor.superhoppers.stored.Stored;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) ||
				event.getItem() == null || event.getItem().getType() == XMaterial.AIR.parseMaterial())
			return;

		if(Bukkit.getPluginManager().isPluginEnabled("SuperHoppers") && event.getClickedBlock().getType() == Material.HOPPER){
		} else {
			if(event.isCancelled())
				return;
		}

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

		if(item.hasTag("permission") && !player.hasPermission(item.getString("permission"))){
			if(errorSound != null)
				errorSound.play(player);
			plugin.getLocale().getMessage("sellwand.sell-no-permission").sendPrefixedMessage(player);
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
		Map<Integer, ItemStack> items = new HashMap<>();

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
					items.put(0, itemStack);
					if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
						storageChest.setAmount(0);
						storageChest.update();
					}
				}
			}
		} else if(Bukkit.getPluginManager().isPluginEnabled("SuperHoppers") && event.getClickedBlock().getType() == Material.HOPPER){
			SuperHopper<?> hopper = SuperHoppersAPI.getHopperManager().getFromLocation(event.getClickedBlock().getLocation());

			if(hopper == null) return;

			if(!hopper.getType().equals("Item"))
				return;

			event.setCancelled(true);

			final double[] price = {0D};
			final int[] itemAmounts = {0};

			List<? extends Stored<?>> hopperStorage = hopper.getStorage();

			for (int i = 0; i < hopperStorage.size(); i++) {
				Stored<?> storedItem = hopperStorage.get(i);
				ItemStack itemStack = storedItem.asItem(player);
				double itemPrice = ShopManager.getSellPrice(player, itemStack, Integer.parseInt(String.valueOf(storedItem.getAmount())));
				itemPrice = itemPrice * Integer.parseInt(String.valueOf(storedItem.getAmount()));
				if(itemPrice >= 0D) {
					price[0] += itemPrice;
					itemAmounts[0] += Integer.parseInt(String.valueOf(storedItem.getAmount()));
					items.put(i, itemStack);
					if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
						storedItem.setAmount(0);
						hopper.updateHologram();
						hopper.updateStorageIfView();
					}
				}
			}

			amount += price[0];
			itemAmount += itemAmounts[0];

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
					items.put(i, chestItem);

					if(event.getAction() == Action.RIGHT_CLICK_BLOCK) ShopManager.sellItem(player, chestItem, chestItem.getAmount());
				}
			}
		}
		double multiplier = item.getDouble("Multiplier");
		amount = amount * multiplier;

		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// Hologram
			Location hologramLoc = null;
			hologramLoc = event.getClickedBlock().getLocation();
			hologramLoc.setDirection(player.getLocation().getDirection());

			double offsetx = player.getLocation().getX() - hologramLoc.getX();
			double offsetz = player.getLocation().getZ() - hologramLoc.getZ();

			hologramLoc.add(new Vector(offsetx, 0, offsetz).normalize().multiply(1));
			hologramLoc.subtract(0, 0.8, 0);

			// Call hologram event
			SellwandHologramEvent sellwandHologramEvent = new SellwandHologramEvent(player, hologramLoc, itemAmount, amount, items);
			Bukkit.getPluginManager().callEvent(sellwandHologramEvent);

			if (sellwandHologramEvent.isCancelled())
				return;

			if(sellwandHologramEvent.getSellPrice() != amount)
				amount = sellwandHologramEvent.getSellPrice();

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

			// Call sell event
			SellwandSellEvent sellwandSellEvent = new SellwandSellEvent(player, uses, itemAmount, amount, items);
			Bukkit.getPluginManager().callEvent(sellwandSellEvent);

			if (sellwandSellEvent.isCancelled())
				return;

			if(sellwandSellEvent.getSellPrice() != amount)
				amount = sellwandSellEvent.getSellPrice();

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
				if (uses != -1)
					--uses;
				Object hand = null;
				if(!ReflectionUtils.getVersion().contains("1_8"))
					hand = event.getHand();
				if (plugin.getConfig().getBoolean("Destroy wand") && uses == 0)
					PlayerInventoryUtils.setInActiveHand(player, hand, null);
				else
					PlayerInventoryUtils.setInActiveHand(player, hand, plugin.getSellWandManager().getSellwand((item.getString("id").isEmpty() ? "old" : item.getString("id")))
							.getSellWand(uses, (item.getInteger("total_item") + itemAmount), (item.getDouble("total_sold_price") + amount)));
				plugin.getLocale().getMessage("sellwand.sell-success")
						.processPlaceholder("price", EconomyManager.formatEconomy(amount))
						.processPlaceholder("item_amount", String.valueOf(itemAmount))
						.sendPrefixedMessage(player);
				if(!PermissionUtils.hasPermission(player, "sellwand.cooldown.bypass")){
					int cooldown = PermissionUtils.getNumberFromPermission(player, "sellwand.cooldown", false, 0);
					CooldownBuilder.addCooldown("Use cooldown", player.getUniqueId(), cooldown);
				}
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
}

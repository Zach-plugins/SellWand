package me.zachary.sellwand.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.hooks.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class LeftClickListener implements Listener {
    private Sellwand plugin;

    public LeftClickListener(Sellwand plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event){
        if(event.getAction() != Action.LEFT_CLICK_BLOCK || event.getHand() == EquipmentSlot.OFF_HAND || event.getClickedBlock().getBlockData().getMaterial() != Material.CHEST)
            return;
        Player player = event.getPlayer();
        Chest chestClicked = (Chest) event.getClickedBlock().getState();
        NBTItem item = null;
        double amount = 0D;
        int itemAmount = 0;
        if(event.getItem() != null)
            item = new NBTItem(event.getItem());
        if(item != null && item.getBoolean("Is a sell wand")){
            event.setCancelled(true);
            double multiplier = item.getDouble("Multiplier");
            for (int i = 0; i < chestClicked.getInventory().getContents().length; i++) {
                ItemStack chestItem = chestClicked.getInventory().getItem(i);
                Double price = 0D;
                if (chestItem != null)
                    price = plugin.getItemPrice(player, chestItem);
                if (price != -1.0 && chestItem != null) {
                    itemAmount += chestItem.getAmount();
                    amount += price;
                }
            }
            amount = amount * multiplier;
            Location hologramLoc = event.getClickedBlock().getLocation().add(0 , -0.80, 1);
            HologramManager.createHologram(hologramLoc, getHologramLine(itemAmount, amount));
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    HologramManager.removeHologram(hologramLoc);
                }
            }, 150L);
        }
    }

    public List<String> getHologramLine(int amount, double price){
        List<String> line = new ArrayList<>();
        for (String i : plugin.getConfig().getStringList("Hologram line")) {
            line.add(i.replace("%amount%", String.valueOf(amount))
                    .replace("%price%", EconomyManager.formatEconomy(price)));
        }
        return line;
    }
}

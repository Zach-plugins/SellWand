package me.zachary.sellwand.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.utils.*;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.hooks.ShopManager;
import me.zachary.zachcore.utils.items.ItemBuilder;
import me.zachary.zachcore.utils.xseries.XMaterial;
import nl.rutgerkok.blocklocker.BlockLockerAPIv2;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class RightClickListener implements Listener {
    private Sellwand plugin;

    public RightClickListener(Sellwand plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if(!ReflectionUtils.getVersion().contains("1_8"))
            if(event.getHand() == EquipmentSlot.OFF_HAND)
                return;
        Player player = event.getPlayer();
        NBTItem item = null;
        double amount = 0D;
        int itemAmount = 0;
        if(event.getItem() != null)
            item = new NBTItem(event.getItem());
        if(item != null && item.getBoolean("Is a sell wand")){
            if(plugin.getConfig().getBoolean("Use WorldGuard protection") &&
                    Bukkit.getPluginManager().getPlugin("WorldGuard") != null &&
                    !WorldGuardUtils.canAccessChest(player))
                return;
            if(Bukkit.getPluginManager().getPlugin("BlockLocker") != null && BlockLockerAPIv2.isProtected(event.getClickedBlock()) && !BlockLockerAPIv2.isOwner(player, event.getClickedBlock()))
                return;
            Inventory contents = StorageUtils.getStorageContents(event.getClickedBlock());
            if(contents == null || contents.getContents() == null)
                return;
            CooldownBuilder.createCooldown("Use cooldown");
            if(!player.hasPermission("sellwand.use")){
                MessageUtils.sendMessage(player, plugin.getMessage().getString("No permission"));
                return;
            }
            int uses = item.getInteger("Uses");
            if(uses == 0)
                return;
            event.setCancelled(true);
            if(CooldownBuilder.isCooldown("Use cooldown", player.getUniqueId())){
                MessageUtils.sendMessage(player, plugin.getMessage().getString("Player cooldown").replace("%seconds%", String.valueOf(CooldownBuilder.getCooldown("Use cooldown", player.getUniqueId()) / 1000)));
                return;
            }
            double multiplier = item.getDouble("Multiplier");
            for (int i = 0; i < contents.getContents().length; i++){
                ItemStack chestItem = contents.getItem(i);
                Double price = 0D;
                if(chestItem != null)
                    price = ShopManager.getSellPrice(player, chestItem, chestItem.getAmount());
                if(price != -1.0 && chestItem != null){
                    contents.setItem(i, new ItemBuilder(XMaterial.AIR.parseMaterial()).build());
                    itemAmount += chestItem.getAmount();
                    amount += price;
                }
            }
            amount = amount * multiplier;
            if(plugin.getConfig().getBoolean("Log sell with sell wand in console") && amount != 0D)
                plugin.getLog().log("Player " + player.getName() + " sell " + itemAmount + " items for a total of " + EconomyManager.formatEconomy(amount));
            if(amount != 0D){
                EconomyManager.deposit(player, amount);
                if(uses != -1){
                    uses = uses - 1;
                    if(plugin.getConfig().getBoolean("Destroy wand") && uses == 0)
                        PlayerInventoryUtils.SetInMainHand(player, null);
                    else
                        PlayerInventoryUtils.SetInMainHand(player, plugin.getSellWandBuilder().getSellWand(1, multiplier, uses));
                }
                MessageUtils.sendMessage(player, plugin.getMessage().getString("Amount give")
                        .replace("%price%", EconomyManager.formatEconomy(amount))
                        .replace("%item_amount%", String.valueOf(itemAmount)));
                int cooldown = PermissionUtils.getNumberFromPermission(player, "sellwand.cooldown", false, 0);
                CooldownBuilder.addCooldown("Use cooldown", player.getUniqueId(), cooldown);
            }
            else
                MessageUtils.sendMessage(player, plugin.getMessage().getString("No item to sell in chest"));
        }
    }
}

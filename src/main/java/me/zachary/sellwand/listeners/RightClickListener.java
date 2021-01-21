package me.zachary.sellwand.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.utils.CooldownBuilder;
import me.zachary.zachcore.utils.MessageUtils;
import me.zachary.zachcore.utils.PermissionUtils;
import me.zachary.zachcore.utils.PlayerInventoryUtils;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.items.ItemBuilder;
import me.zachary.zachcore.utils.xseries.XMaterial;
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
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        Player player = event.getPlayer();
        Inventory contents = null;
        List<Material> shulkerBoxMaterial = Arrays.asList(
                Material.SHULKER_BOX,
                Material.WHITE_SHULKER_BOX,
                Material.ORANGE_SHULKER_BOX,
                Material.MAGENTA_SHULKER_BOX,
                Material.LIGHT_BLUE_SHULKER_BOX,
                Material.YELLOW_SHULKER_BOX,
                Material.LIME_SHULKER_BOX,
                Material.PINK_SHULKER_BOX,
                Material.GRAY_SHULKER_BOX,
                Material.LIGHT_BLUE_SHULKER_BOX,
                Material.CYAN_SHULKER_BOX,
                Material.PURPLE_SHULKER_BOX,
                Material.BLUE_SHULKER_BOX,
                Material.BROWN_SHULKER_BOX,
                Material.GREEN_SHULKER_BOX,
                Material.CYAN_SHULKER_BOX,
                Material.RED_SHULKER_BOX,
                Material.BLACK_SHULKER_BOX
        );
        if(event.getClickedBlock().getBlockData().getMaterial() == Material.CHEST
                || event.getClickedBlock().getBlockData().getMaterial() == Material.TRAPPED_CHEST){
            Chest chests = (Chest) event.getClickedBlock().getState();
            contents = chests.getInventory();
        }else if(shulkerBoxMaterial.contains(event.getClickedBlock().getBlockData().getMaterial())){
            ShulkerBox shulkerBox = (ShulkerBox) event.getClickedBlock().getState();
            contents = shulkerBox.getInventory();
        }else if(event.getClickedBlock().getBlockData().getMaterial() == Material.BARREL){
            Barrel barrel = (Barrel) event.getClickedBlock().getState();
            contents = barrel.getInventory();
        }
        if(contents == null || contents.getContents() == null)
            return;
        NBTItem item = null;
        double amount = 0D;
        int itemAmount = 0;
        if(event.getItem() != null)
            item = new NBTItem(event.getItem());
        if(item != null && item.getBoolean("Is a sell wand")){
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
                    price = plugin.getItemPrice(player, chestItem);
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
                PlayerInventoryUtils.SetInMainHand(player, plugin.getSellWandBuilder().getSellWand(1, multiplier, (uses - 1)));
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

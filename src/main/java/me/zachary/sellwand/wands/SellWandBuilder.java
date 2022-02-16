package me.zachary.sellwand.wands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.utils.items.ItemBuilder;
import me.zachary.zachcore.utils.xseries.XMaterial;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SellWandBuilder {
    private Sellwand plugin;

    public SellWandBuilder(Sellwand plugin) {
        this.plugin = plugin;
    }

    public ItemStack getSellWand(int amount, double multiplier, int uses){
        ItemBuilder sellWand = new ItemBuilder(XMaterial.valueOf(plugin.getConfig().getString("Sell wand.Item")).parseMaterial())
                .name(plugin.getConfig().getString("Sell wand.Name").replace("%multiplier%", String.valueOf(multiplier)))
                .amount(amount)
                .lore(getLore(multiplier, uses));
        if(plugin.getConfig().getBoolean("Sell wand.Glowing"))
            sellWand.enchant(Enchantment.ARROW_INFINITE, 1).flag(ItemFlag.HIDE_ENCHANTS);
        NBTItem sellWandNBT = new NBTItem(sellWand.build());
        sellWandNBT.setBoolean("Is a sell wand", true);
        sellWandNBT.setDouble("Multiplier", multiplier);
        sellWandNBT.setInteger("Uses", uses);
        return sellWandNBT.getItem();
    }

    public List<String> getLore(double multiplier, int uses){
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("Sell wand.Lore")) {
            lore.add(line.replace("%multiplier%", String.valueOf(multiplier))
                    .replace("%uses%", String.valueOf(uses >= 0 ? uses : plugin.getConfig().getString("Sell wand.Infinite"))));
        }
        return lore;
    }
}

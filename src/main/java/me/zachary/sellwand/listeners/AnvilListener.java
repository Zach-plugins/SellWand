package me.zachary.sellwand.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {
    private final Sellwand plugin;

    public AnvilListener(Sellwand plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAnvilEvent(PrepareAnvilEvent event) {
        ItemStack itemStack = event.getInventory().getItem(0);

        if (itemStack == null)
            return;

        NBTItem item = new NBTItem(itemStack);
        if (!(item.hasKey("Is a sell wand") || item.hasKey("UUID_Sellwand")))
            return;

        event.setResult(null);
    }
}

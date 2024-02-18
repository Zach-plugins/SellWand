package me.zachary.sellwand.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;

public class GrindstoneListener implements Listener {
    private final Sellwand plugin;

    public GrindstoneListener(Sellwand plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onGrindstoneEvent(PrepareGrindstoneEvent event) {
        ItemStack itemStack = event.getInventory().getItem(0);

        if (itemStack == null)
            return;

        NBTItem item = new NBTItem(itemStack);
        if (!(item.hasKey("Is a sell wand") || item.hasKey("UUID_Sellwand")))
            return;

        event.setResult(null);
    }
}

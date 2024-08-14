package me.zachary.sellwand.confirmations;

import me.zachary.sellwand.Sellwand;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationManager {
    private Sellwand plugin;
    private List<Confirmation> confirmations;
    
    public ConfirmationManager(Sellwand plugin) {
        this.plugin = plugin;
        confirmations = new ArrayList<>();
    }
    
    public void addConfirmation(Confirmation confirmation) {
        confirmation.spawnParticle();
        Bukkit.getScheduler().runTaskLater(Sellwand.getInstance(), () -> {
            removeConfirmation(confirmation);
            Bukkit.getScheduler().cancelTask(confirmation.getTaskid());
        }, (Sellwand.getInstance().getConfig().getInt("Confirmation.time", 2) * 20L));
        confirmations.add(confirmation);
    }
    
    public void removeConfirmation(Confirmation confirmation) {
        Bukkit.getScheduler().cancelTask(confirmation.getTaskid());
        confirmations.remove(confirmation);
    }
    
    public Confirmation getConfirmationByBlock(Block block) {
        for (Confirmation confirmation : confirmations) {
            if (confirmation.getBlock().equals(block))
                return confirmation;
        }
        return null;
    }
}

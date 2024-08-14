package me.zachary.sellwand.confirmations;

import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.dependencies.com.cryptomorin.xseries.particles.XParticle;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Confirmation {
    private Player player;
    private Block block;
    private int taskid;
    
    public Confirmation(Player player, Block block) {
        this.player = player;
        this.block = block;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Block getBlock() {
        return block;
    }
    
    public int getTaskid() {
        return taskid;
    }
    
    public void spawnParticle() {
        Particle particle = XParticle.DUST.get();
        String[] color = Sellwand.getInstance().getConfig().getString("Confirmation.color", "0,255,0").split(",");
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])), 1);

        taskid = Bukkit.getScheduler().runTaskTimerAsynchronously(Sellwand.getInstance(), () -> {
            for (float i = 0; i <= 1; i += 0.1F) {
                block.getWorld().spawnParticle(particle, block.getLocation().add(0, i, 0), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(1, i, 0), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(0, i, 1), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(1, i, 1), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(i, 0, 0), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(i, 1, 0), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(i, 0, 1), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(i, 1, 1), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(0, 0, i), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(0, 1, i), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(1, 0, i), 2, dustOptions);
                block.getWorld().spawnParticle(particle, block.getLocation().add(1, 1, i), 2, dustOptions);
            }
        }, 0, 10).getTaskId();
    }
}

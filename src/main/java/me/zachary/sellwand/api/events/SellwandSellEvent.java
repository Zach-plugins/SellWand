package me.zachary.sellwand.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SellwandSellEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean isCancelled;
    private final Player player;
    private int sellwandUses, itemAmount;
    private double sellPrice;
    private Map<Integer, ItemStack> items;

    public SellwandSellEvent(Player player, int sellwandUses, int itemAmount, double sellPrice, Map<Integer, ItemStack> items) {
        this.player = player;
        this.sellwandUses = sellwandUses;
        this.itemAmount = itemAmount;
        this.sellPrice = sellPrice;
        this.items = items;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    /**
     * @return the player who sell the item
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the amount of Uses left on the sellwand
     */
    public int getSellwandUses() {
        return sellwandUses;
    }

    /**
     * @return the amount of item sold
     */
    public int getItemAmount() {
        return itemAmount;
    }

    /**
     * @return the price of the item sold in total
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * @return the items sold
     */
    public Map<Integer, ItemStack> getItems() {
        return items;
    }
}

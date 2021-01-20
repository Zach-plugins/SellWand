package me.zachary.sellwand;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import fr.maxlego08.shop.api.ShopManager;
import me.zachary.sellwand.commands.GiveCommand;
import me.zachary.sellwand.commands.ReloadCommand;
import me.zachary.sellwand.files.MessageFile;
import me.zachary.sellwand.listeners.LeftClickListener;
import me.zachary.sellwand.listeners.RightClickListener;
import me.zachary.sellwand.wands.SellWandBuilder;
import me.zachary.updatechecker.Updatechecker;
import me.zachary.zachcore.ZachCorePlugin;
import me.zachary.zachcore.utils.Metrics;
import me.zachary.zachcore.utils.hooks.EconomyManager;
import me.zachary.zachcore.utils.hooks.HologramManager;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.quantumshop.ShopAPI;
import su.nightexpress.quantumshop.modules.list.gui.types.BuyType;

public final class Sellwand extends ZachCorePlugin {
    ShopManager manager;

    @Override
    public void onEnable() {
        int pluginId = 9724;
        Metrics metrics = new Metrics(this, pluginId);
        preEnable();
        EconomyManager.load();
        HologramManager.load(this);
        Updatechecker.updateSongoda(this, 543);
        saveDefaultConfig();

        // Load zShop
        if(Bukkit.getPluginManager().getPlugin("zShop") != null)
            manager = getProvider(ShopManager.class);

        // Load Message file.
        new MessageFile(this);

        // Load listeners
        new RightClickListener(this);
        new LeftClickListener(this);

        // Load Commands
        new GiveCommand(this);
        new ReloadCommand(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public MessageFile getMessage() {
        return new MessageFile(this);
    }

    public SellWandBuilder getSellWandBuilder(){
        return new SellWandBuilder(this);
    }

    public Double getItemPrice(Player player, ItemStack itemStack){
        final double[] amount = {-1.0};
        if(getConfig().getString("Item price.Choice").equals("Custom")){
            for (String item : getConfig().getStringList("Item price.Custom")) {
                String[] split = item.split(",");
                if(new ItemStack(Material.valueOf(split[0])).isSimilar(itemStack)){
                    return Double.parseDouble(split[1]) * itemStack.getAmount();
                }
            }
            return -1.0;
        }else if(getConfig().getString("Item price.Choice").equals("ShopGUIPlus") && Bukkit.getPluginManager().getPlugin("ShopGUIPlus") != null){
            return ShopGuiPlusApi.getItemStackPriceSell(player, itemStack);
        }else if(getConfig().getString("Item price.Choice").equals("zShop") && Bukkit.getPluginManager().getPlugin("zShop") != null){
            if(manager.getItemButton(itemStack).isPresent() && manager.getItemButton(itemStack).get().getSellPrice(player) != 0)
                return manager.getItemButton(itemStack).get().getSellPrice(player);
            return -1.0;
        }else if(getConfig().getString("Item price.Choice").equals("CMI") && Bukkit.getPluginManager().getPlugin("CMI") != null){
            WorthItem worth = CMI.getInstance().getWorthManager().getWorth(itemStack);
            if(worth == null)
                return -1.0;
            return worth.getSellPrice() * itemStack.getAmount();
        }else if(getConfig().getString("Item price.Choice").equals("QuantumShop") && Bukkit.getPluginManager().getPlugin("QuantumShop") != null){
            ShopAPI.getGUIShop().getShops().forEach(shopGUI -> {
                shopGUI.getProducts().forEach((s, shopProduct) -> {
                    if(shopProduct.getBuyItem().getType().name().equals(itemStack.getType().name())){
                        if(shopProduct.getPossibleQuantity(player, BuyType.SELL) >= itemStack.getAmount()){
                            amount[0] = (shopProduct.getSellPrice() * itemStack.getAmount());
                        }
                    }
                });
            });
            return amount[0];
        }
        return -1.0;
    }

    protected <T> T getProvider(Class<T> classz) {
        RegisteredServiceProvider<T> provider = getServer().getServicesManager().getRegistration(classz);
        if (provider == null)
            return null;
        return provider.getProvider() != null ? (T) provider.getProvider() : null;
    }
}

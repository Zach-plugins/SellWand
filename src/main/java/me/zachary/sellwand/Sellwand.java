package me.zachary.sellwand;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import fr.maxlego08.shop.api.ShopManager;
import me.gypopo.economyshopgui.EconomyShopGUI;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.quantumshop.ShopAPI;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Sellwand extends ZachCorePlugin {
    ShopManager manager;
    private Map<Material, Double> itemPrice = new HashMap<Material, Double>();

    @Override
    public void onEnable() {
        int pluginId = 9724;
        Metrics metrics = new Metrics(this, pluginId);
        EconomyManager.load();
        HologramManager.load(this);
        Updatechecker.update(this, "sellwand");
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
        if(getConfig().getString("Item price.Choice").equals("EconomyShopGUI") && Bukkit.getPluginManager().getPlugin("EconomyShopGUI") != null)
            loadEconomyShopGUI();
        preEnable(this);
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
                        amount[0] = (shopProduct.getSellPrice() * itemStack.getAmount());
                    }
                });
            });
            return amount[0];
        } else if(getConfig().getString("Item price.Choice").equals("EconomyShopGUI") && Bukkit.getPluginManager().getPlugin("EconomyShopGUI") != null){
            itemPrice.forEach((material, aDouble) -> {
                if(itemStack.getType() == material)
                    amount[0] = aDouble * itemStack.getAmount();
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

    public void loadEconomyShopGUI(){
        YamlConfiguration shopConfig = EconomyShopGUI.getInstance().loadConfiguration(new File(getDataFolder().getParent() + "/EconomyShopGUI/shops.yml"), "shops.yml");
        for(String s : shopConfig.getKeys(false)){
            ConfigurationSection configurationSection = shopConfig.getConfigurationSection(s);
            for(String item : configurationSection.getKeys(false)){
                itemPrice.put(Material.valueOf(configurationSection.getString(item + ".material")), configurationSection.getDouble(item + ".sell"));
            }
        }
        getLog().log("Loaded " + itemPrice.size() + " items from EconomyShopGUI.");
    }

    @Override
    public void onDataLoad() {

    }
}

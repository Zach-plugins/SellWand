package me.zachary.sellwand;

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
import me.zachary.zachcore.utils.hooks.ShopManager;

public final class Sellwand extends ZachCorePlugin {

    @Override
    public void onEnable() {
        int pluginId = 9724;
        Metrics metrics = new Metrics(this, pluginId);
        EconomyManager.load();
        HologramManager.load(this);
        Updatechecker.update(this, "sellwand");
        saveDefaultConfig();

        ShopManager.load(this);

        // Load Message file.
        new MessageFile(this);

        // Load listeners
        new RightClickListener(this);
        new LeftClickListener(this);

        // Load Commands
        new GiveCommand(this);
        new ReloadCommand(this);
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

    @Override
    public void onDataLoad() {

    }
}

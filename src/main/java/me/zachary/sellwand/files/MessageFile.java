package me.zachary.sellwand.files;

import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.storage.YAMLFile;

public class MessageFile {
    private Sellwand plugin;
    private YAMLFile cfg;

    public MessageFile(Sellwand plugin) {
        this.plugin = plugin;
        this.cfg = new YAMLFile(plugin.getDataFolder(), "message.yml");
        this.loadDefaults();
    }

    private void loadDefaults() {
        this.cfg.getConfig().options().header("This file is auto-reload.");
        this.cfg.getConfig().options().copyHeader(true);
        this.cfg.add("No permission", "&cYou don't have permission to do that.");
        this.cfg.add("Player not found", "&cThis player is not connected or not exist.");
        this.cfg.add("Successful give", "&6You have successful receive a sell wand.");
        this.cfg.add("Successful reload", "&cYou have successful reload the config.");
        this.cfg.add("No item to sell in chest", "&6No item to sell in this chest.");
        this.cfg.add("Amount give", "&6You have been sell %item_amount% items of chest content for a total of &e%price%&6.");
    }

    public String getString(String path){
        return this.cfg.getString(path);
    }
}

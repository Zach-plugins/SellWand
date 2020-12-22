package me.zachary.sellwand.commands;

import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.commands.Command;
import me.zachary.zachcore.commands.CommandResult;
import me.zachary.zachcore.utils.MessageUtils;
import org.bukkit.entity.Player;

public class ReloadCommand extends Command {
    private Sellwand plugin;

    public ReloadCommand(Sellwand plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommand() {
        return "sellwandreload";
    }

    @Override
    public CommandResult onPlayerExecute(Player player, String[] strings) {
        if(!player.hasPermission("sellwand.reload")){
            MessageUtils.sendMessage(player, plugin.getMessage().getString("No permission"));
            return CommandResult.COMPLETED;
        }
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        MessageUtils.sendMessage(player, plugin.getMessage().getString("Successful reload"));
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(boolean b, String[] strings) {
        return CommandResult.COMPLETED;
    }
}

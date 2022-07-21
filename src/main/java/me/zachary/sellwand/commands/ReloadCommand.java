package me.zachary.sellwand.commands;

import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.commands.Command;
import me.zachary.zachcore.commands.CommandResult;
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
		if (!player.hasPermission("sellwand.reload")) {
			plugin.getLocale().getMessage("command.no-permission").sendPrefixedMessage(player);
			return CommandResult.COMPLETED;
		}
		plugin.reload();
		plugin.getLocale().getMessage("command.reload").sendPrefixedMessage(player);
		return CommandResult.COMPLETED;
	}

	@Override
	public CommandResult onConsoleExecute(boolean b, String[] strings) {
		return CommandResult.COMPLETED;
	}
}

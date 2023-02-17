package me.zachary.sellwand.commands;

import me.zachary.sellwand.Sellwand;
import me.zachary.sellwand.wands.OSellwand;
import me.zachary.zachcore.commands.Command;
import me.zachary.zachcore.commands.CommandResult;
import me.zachary.zachcore.utils.MessageUtils;
import me.zachary.zachcore.utils.PlayerInventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GiveCommand extends Command {
	private Sellwand plugin;

	public GiveCommand(Sellwand plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getCommand() {
		return "sellwandgive";
	}

	@Override
	public CommandResult onPlayerExecute(Player player, String[] strings) {
		if (!player.hasPermission("sellwand.give")) {
			plugin.getLocale().getMessage("command.no-permission").sendPrefixedMessage(player);
			return CommandResult.COMPLETED;
		}
		Player target = null;
		if (strings.length < 2) {
			MessageUtils.sendMessage(player, "&cInvalid usage. &eCorrect usage: &6/sellwandgive <player> <sellwand>");
			return CommandResult.COMPLETED;
		}
		target = Bukkit.getPlayer(strings[0]);
		if (target == null) {
			plugin.getLocale().getMessage("command.player-not-found")
					.processPlaceholder("player", strings[0])
					.sendPrefixedMessage(player);
			return CommandResult.COMPLETED;
		}
		OSellwand sellwand = plugin.getSellWandManager().getSellwand(strings.length > 3 ? "old" : strings[1]);

		if (sellwand == null) {
			plugin.getLocale().getMessage("command.sellwand-not-found")
					.processPlaceholder("sellwand", strings[1])
					.sendPrefixedMessage(player);
			return CommandResult.COMPLETED;
		}

		PlayerInventoryUtils.giveItem(target, sellwand.getSellWand(), true);


		plugin.getLocale().getMessage("command.give-success")
				.processPlaceholder("player", target.getName())
				.sendPrefixedMessage(player);
		plugin.getLocale().getMessage("command.receive-success")
				.processPlaceholder("sellwand_name", sellwand.getName())
				.sendPrefixedMessage(target);
		return CommandResult.COMPLETED;
	}

	@Override
	public CommandResult onConsoleExecute(boolean b, String[] strings) {
		if (strings.length < 2) {
			System.out.println("sellwandgive <player> <sellwand>");
			return CommandResult.COMPLETED;
		}
		Player target = Bukkit.getPlayer(strings[0]);
		if (target == null) {
			System.out.println("Player not found");
			return CommandResult.COMPLETED;
		}
		OSellwand sellwand = plugin.getSellWandManager().getSellwand(strings.length > 3 ? "old" : strings[1]);

		if (sellwand == null) {
			plugin.getLocale().getMessage("command.sellwand-not-found")
					.processPlaceholder("sellwand", strings[1])
					.sendPrefixedMessage(Bukkit.getConsoleSender());
			return CommandResult.COMPLETED;
		}

		PlayerInventoryUtils.giveItem(target, sellwand.getSellWand(), true);
		plugin.getLocale().getMessage("command.give-success")
				.processPlaceholder("player", target.getName())
				.sendPrefixedMessage(Bukkit.getConsoleSender());

		plugin.getLocale().getMessage("command.receive-success")
				.processPlaceholder("sellwand_name", sellwand.getName())
				.sendPrefixedMessage(target);
		return CommandResult.COMPLETED;
	}

	@Override
	public List<String> getCommandComplete(Player player, String alias, String[] args) {
		List<String> arg = new ArrayList<>();
		if (args.length == 1) {
			arg.addAll(Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
		} else if (args.length == 2) {
			arg.addAll(plugin.getSellWandManager().getSellwands().stream().map(OSellwand::getId).collect(Collectors.toList()));
		}
		return arg;
	}
}

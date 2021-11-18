package me.zachary.sellwand.commands;

import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.commands.Command;
import me.zachary.zachcore.commands.CommandResult;
import me.zachary.zachcore.utils.MessageUtils;
import me.zachary.zachcore.utils.PlayerInventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
        if(!player.hasPermission("sellwand.give")){
            MessageUtils.sendMessage(player, plugin.getMessage().getString("No permission"));
            return CommandResult.COMPLETED;
        }
        Player target = null;
        int amount = 0;
        int uses = 0;
        double multiplier = 0D;
        if(strings.length < 4){
            MessageUtils.sendMessage(player, "&cInvalid usage. &eCorrect usage: &6/sellwandgive <user> <amount> <uses> <multiplier>");
            return CommandResult.COMPLETED;
        }
        target = Bukkit.getPlayer(strings[0]);
        try {
            amount = Integer.parseInt(strings[1]);
            uses = Integer.parseInt(strings[2]);
            multiplier = Double.parseDouble(strings[3]);
        }catch (Exception e){
            MessageUtils.sendMessage(player, "&cInvalid usage. &eCorrect usage: &6/sellwandgive <user> <amount> <uses> <multiplier>");
        }
        if(target == null){
            MessageUtils.sendMessage(player, plugin.getMessage().getString("Player not found"));
            return CommandResult.COMPLETED;
        }

        PlayerInventoryUtils.giveItem(target, plugin.getSellWandBuilder().getSellWand(amount, multiplier, uses), true);
        MessageUtils.sendMessage(target, plugin.getMessage().getString("Successful give"));
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(boolean b, String[] strings) {
        if(strings.length < 4){
            System.out.println("sellwandgive <user> <amount> <uses> <multiplier>");
            return CommandResult.COMPLETED;
        }
        Player target = Bukkit.getPlayer(strings[0]);
        if(target == null){
            System.out.println("Player not found");
            return CommandResult.COMPLETED;
        }
        int amount = 0;
        int uses = 0;
        double multiplier = 0D;
        try {
            amount = Integer.parseInt(strings[1]);
            uses = Integer.parseInt(strings[2]);
            multiplier = Double.parseDouble(strings[3]);
        }catch (Exception e){
            System.out.println("Invalid usage. Correct usage: /sellwandgive <user> <amount> <uses/\"-1\"=infinite uses> <multiplier>");
        }

        PlayerInventoryUtils.giveItem(target, plugin.getSellWandBuilder().getSellWand(amount, multiplier, uses), true);
        MessageUtils.sendMessage(target, plugin.getMessage().getString("Successful give"));
        return CommandResult.COMPLETED;
    }

    @Override
    public List<String> getCommandComplete(Player player, String alias, String[] args) {
        List<String> arg = new ArrayList<>();
        if(args.length == 1){
            Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
            Bukkit.getServer().getOnlinePlayers().toArray(players);
            for (int i = 0; i < players.length; i++) {
                arg.add(players[i].getName());
            }
        }else if(args.length == 2){
            arg.add("1");
        }else if(args.length == 3){
            arg.add("100");
        }else if(args.length == 4){
            arg.add("1.0");
        }
        return arg;
    }
}

package me.zachary.sellwand.commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zachary.sellwand.Sellwand;
import me.zachary.zachcore.commands.Command;
import me.zachary.zachcore.commands.CommandResult;
import me.zachary.zachcore.utils.MessageUtils;
import me.zachary.zachcore.utils.PlayerInventoryUtils;
import me.zachary.zachcore.utils.ReflectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModifyCommand extends Command {
    private Sellwand plugin;

    public ModifyCommand(Sellwand plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommand() {
        return "sellwandmodify";
    }

    @Override
    public CommandResult onPlayerExecute(Player player, String[] strings) {
        if (!player.hasPermission("sellwand.modify")) {
            plugin.getLocale().getMessage("command.no-permission").sendPrefixedMessage(player);
            return CommandResult.COMPLETED;
        }

        if(strings.length == 0){
            MessageUtils.sendMessage(player, "&c/sellwandmodify <uses|item|amount> <add|set> <amount>");
            return CommandResult.COMPLETED;
        }

        ItemStack itemStack = player.getItemInHand();
        if(itemStack == null || itemStack.getType().isAir()){
            plugin.getLocale().getMessage("modify.no-sellwand").sendPrefixedMessage(player);
            return CommandResult.COMPLETED;
        }

        NBTItem nbtItem = new NBTItem(itemStack);
        if (!(nbtItem.hasKey("Is a sell wand") || nbtItem.hasKey("UUID_Sellwand"))){
            plugin.getLocale().getMessage("modify.no-sellwand").sendPrefixedMessage(player);
            return CommandResult.COMPLETED;
        }

        if(strings.length < 3){
            MessageUtils.sendMessage(player, "&c/sellwandmodify <uses|item|amount> <add|set> <amount>");
            return CommandResult.COMPLETED;
        }

        String action = strings[1].toLowerCase();

        if(!action.equals("add") && !action.equals("set")){
            MessageUtils.sendMessage(player, "&c/sellwandmodify <uses|item|amount> <add|set> <amount>");
            return CommandResult.COMPLETED;
        }

        switch (strings[0].toLowerCase()) {
            case "uses":
                if(!strings[2].matches("(-|)[0-9]+")){
                    plugin.getLocale().getMessage("modify.amount-invalid").sendPrefixedMessage(player);
                    return CommandResult.COMPLETED;
                }
                int uses = nbtItem.getInteger("Uses");

                if(action.equals("add"))
                    uses += Integer.parseInt(strings[2]);
                else
                    uses = Integer.parseInt(strings[2]);

                PlayerInventoryUtils.setInMainHand(player, plugin.getSellWandManager().getSellwand((nbtItem.getString("id")))
                        .getSellWand(uses, (nbtItem.getInteger("total_item")), (nbtItem.getDouble("total_sold_price"))));

                plugin.getLocale().getMessage("modify.success").sendPrefixedMessage(player);
                break;
            case "item":
                if(!strings[2].matches("(-|)[0-9]+")){
                    plugin.getLocale().getMessage("modify.amount-invalid").sendPrefixedMessage(player);
                    return CommandResult.COMPLETED;
                }
                int item = nbtItem.getInteger("total_item");

                if(action.equals("add"))
                    item += Integer.parseInt(strings[2]);
                else
                    item = Integer.parseInt(strings[2]);

                PlayerInventoryUtils.setInMainHand(player, plugin.getSellWandManager().getSellwand((nbtItem.getString("id")))
                        .getSellWand((nbtItem.getInteger("Uses")), item, (nbtItem.getDouble("total_sold_price"))));

                plugin.getLocale().getMessage("modify.success").sendPrefixedMessage(player);
                break;
            case "amount":
                if(!strings[2].matches("(-|)[0-9]+")){
                    plugin.getLocale().getMessage("modify.amount-invalid").sendPrefixedMessage(player);
                    return CommandResult.COMPLETED;
                }
                double amount = nbtItem.getDouble("total_sold_price");

                if(action.equals("add"))
                    amount += Double.parseDouble(strings[2]);
                else
                    amount = Double.parseDouble(strings[2]);

                PlayerInventoryUtils.setInMainHand(player, plugin.getSellWandManager().getSellwand((nbtItem.getString("id")))
                        .getSellWand((nbtItem.getInteger("Uses")), (nbtItem.getInteger("total_item")), amount));

                plugin.getLocale().getMessage("modify.success").sendPrefixedMessage(player);
                break;
            default:
                MessageUtils.sendMessage(player, "&c/sellwandmodify <uses|item|amount> <add|set> <amount>");
                break;
        }

        return CommandResult.COMPLETED;
    }

    @Override
    public List<String> getCommandComplete(Player player, String alias, String[] args) {
        List<String> arg = new ArrayList<>();
        if(args.length == 1) {
            arg.addAll(Arrays.asList("uses", "item", "amount"));
        } else if(args.length == 2) {
            arg.addAll(Arrays.asList("add", "set"));
        }
        return arg;
    }

    @Override
    public CommandResult onConsoleExecute(boolean b, String[] strings) {
        return CommandResult.COMPLETED;
    }
}

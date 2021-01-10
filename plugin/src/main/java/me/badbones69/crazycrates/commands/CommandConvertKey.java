package me.badbones69.crazycrates.commands;

import me.badbones69.crazycrates.Methods;
import me.badbones69.crazycrates.controllers.ui.UIVoucherConverter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandConvertKey implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player target = null;

        if (args.length > 0 && Methods.permCheck(sender, "admin")) {
            target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found " + args[0]);
                return true;
            }
        }

        if (target == null && !(sender instanceof Player)) {
            sender.sendMessage("/convertkey <player>");
            return true;
        } else if (target == null)
            target = (Player) sender;

        new UIVoucherConverter(target);
        return true;
    }

}

package cf.grcq.priveapi.command.defaults;

import cf.grcq.priveapi.command.Command;
import cf.grcq.priveapi.command.CommandHandler;
import cf.grcq.priveapi.command.CommandNode;
import cf.grcq.priveapi.command.parameter.Param;
import cf.grcq.priveapi.utils.Util;
import org.bukkit.command.CommandSender;

public class CommandInfoCommand {

    @Command(names = {"commandinfo", "cmdinfo", "commandinformation"}, permission = "priveapi.command.commandinfo", hidden = true)
    public static void info(CommandSender sender, @Param(name = "command") String command) {
        CommandNode node = CommandHandler.find(command);
        if (node == null) {
            sender.sendMessage(Util.format("&cError: Command '&e" + command + "&c' was not found."));
            return;
        }

        sender.sendMessage(Util.format("&7&m-------------------------"));
        sender.sendMessage(Util.format("&eOwner: &f" + node.getPlugin().getDescription().getName()));
        sender.sendMessage(Util.format("&eDescription: &f" + node.getDescription()));
        sender.sendMessage(Util.format("&eAliases: &f" + String.join(", ", node.getAliases())));
        sender.sendMessage(Util.format("&7&m-------------------------"));

    }

}

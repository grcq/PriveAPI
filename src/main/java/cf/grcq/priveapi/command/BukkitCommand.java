package cf.grcq.priveapi.command;

import cf.grcq.priveapi.utils.Util;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BukkitCommand extends org.bukkit.command.Command {

    private final Command annotation;
    private final CommandNode node;

    public BukkitCommand(Command annotation, CommandNode node) {
        super(node.getName(), "", node.getPermission(), Lists.newArrayList(node.getAliases()));
        this.annotation = annotation;
        this.node = node;

        if (annotation.hidden()) {
            if (node.getPermission().isEmpty()) {
                setPermission(node.getPlugin().getDescription().getName().toLowerCase() + ".command." + node.getName().toLowerCase());
                setPermissionMessage(CommandHandler.getUnknownCommandMessage());
            }
        } else setPermissionMessage(Util.format(CommandHandler.getNoPermissionMessage()));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {

        try {
            node.invoke(sender, args);
        } catch (Exception e) {
            if (sender.isOp()) {
                if (e.getCause() != null) sender.sendMessage(Util.format("&c" + e.getCause().toString()));
                //sender.sendMessage(Util.format("&c" + e.getStackTrace()[0].toString()));
            }

            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (!(sender instanceof Player)) return new ArrayList<>();

        return node.tabComplete(((Player) sender).getPlayer(), args);

    }
}

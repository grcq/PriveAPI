package cf.grcq.bungee.priveapi.command;

import cf.grcq.bungee.priveapi.utils.Util;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BungeeCommand extends net.md_5.bungee.api.plugin.Command {

    private final Command annotation;
    private final CommandNode node;

    @SneakyThrows
    public BungeeCommand(Command annotation, CommandNode node) {
        super(node.getName(), node.getPermission(), node.getAliases());
        this.annotation = annotation;
        this.node = node;

        setPermissionMessage(Util.format(CommandHandler.getNoPermissionMessage()));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        try {
            node.invoke(sender, args);
        } catch (Exception e) {
            sender.sendMessage(Util.format("&cAn unknown error occurred attempting to perform this command."));
            if (sender.hasPermission("priveapi.*")) {
                if (e.getCause() != null) sender.sendMessage(Util.format("&c" + e.getCause().toString()));
            }

            e.printStackTrace();
        }
    }
}

package cf.grcq.bungee.priveapi.command.parameter.defaults;

import cf.grcq.bungee.priveapi.command.parameter.ParameterType;
import cf.grcq.bungee.priveapi.utils.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerParameterType implements ParameterType<ProxiedPlayer> {

    @Override
    public ProxiedPlayer transform(CommandSender sender, String source) {
        if (sender instanceof ProxiedPlayer && source.equalsIgnoreCase("@p")) {
            return (ProxiedPlayer) sender;
        }

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(source);
        if (player != null) {
            return player;
        }

        sender.sendMessage(Util.format("&cError: Player '&e" + source + "&c' was not found."));
        return null;
    }

}

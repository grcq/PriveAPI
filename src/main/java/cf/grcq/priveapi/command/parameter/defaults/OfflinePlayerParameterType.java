package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OfflinePlayerParameterType implements ParameterType<OfflinePlayer> {

    @Override
    public OfflinePlayer transform(CommandSender sender, String source) {
        if (sender instanceof Player && source.equalsIgnoreCase("@p")) {
            return (Player) sender;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(source);
        if (player.hasPlayedBefore()) {
            return player;
        }

        sender.sendMessage(Util.format("&cError: Player '&e" + source + "&c' was not found."));
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String source) {
        List<String> arguments = new ArrayList<>();
        for (Player players : Bukkit.getOnlinePlayers()) {
            arguments.add(players.getName());
        }

        return arguments;
    }

}

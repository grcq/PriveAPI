package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WorldParameterType implements ParameterType<World> {

    @Override
    public World transform(CommandSender sender, String source) {
        World world = Bukkit.getWorld(source);
        if (world != null) return world;

        sender.sendMessage(Util.format("&cError: World '&e" + source + "&c' does not exist."));
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String source) {
        List<String> arguments = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            arguments.add(world.getName());
        }

        return arguments;
    }
}

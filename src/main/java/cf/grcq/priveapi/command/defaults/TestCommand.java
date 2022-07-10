package cf.grcq.priveapi.command.defaults;

import cf.grcq.priveapi.command.Command;
import cf.grcq.priveapi.command.parameter.Param;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand {

    @Command(names = "test")
    public static void test(Player player, @Param(name = "target", defaultValue = "@p") Player target) {
        player.sendMessage(target.getName());
    }

    @Command(names = "test2")
    public static void test2(CommandSender sender, @Param(name = "type") String type, @Param(name = "message", wildcard = true) String message) {
        sender.sendMessage("type: " + type);
        sender.sendMessage("message: " + message);
    }

}

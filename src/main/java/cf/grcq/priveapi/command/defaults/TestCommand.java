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

    @Command(names = "yes")
    public static void yes(CommandSender sender) {
        sender.sendMessage("yes");
    }

    // Does not work, will find a way to create parameters like this one time.
    @Command(names = "yes hello")
    public static void yeshello(CommandSender sender, @Param(name = "integer") Integer integer, @Param(name = "boolean") Boolean bool) {
        sender.sendMessage("Hello there! " + integer + " " + bool);
    }

}

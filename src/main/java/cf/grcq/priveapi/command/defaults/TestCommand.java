package cf.grcq.priveapi.command.defaults;

import cf.grcq.priveapi.command.Command;
import cf.grcq.priveapi.command.parameter.Param;
import org.bukkit.entity.Player;

public class TestCommand {

    @Command(names = "test")
    public static void test(Player player, @Param(name = "target", defaultValue = "@p") Player target) {
        player.sendMessage(target.getName());
    }

}

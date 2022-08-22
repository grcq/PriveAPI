package cf.grcq.priveapi.command.defaults;

import cf.grcq.priveapi.command.Command;
import cf.grcq.priveapi.command.parameter.Param;
import cf.grcq.priveapi.utils.Util;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommands {

    @Command(names = "test", sendUsage = true)
    public static void test(CommandSender sender) {}

    @Command(names = "test hello")
    public static void testHello(Player player) {
        player.sendMessage(Util.format("Hello"));
    }

    @Command(names = "test yes ok")
    public static void testYesOk(Player player, @Param(name = "world") World world) {
        player.sendMessage(world.getName() + " world");
    }

    @Command(names = "test ok")
    public static void testOk(Player player, @Param(name = "test") Player t, @Param(name = "boolean", defaultValue = "0.0") boolean d) {
        player.sendMessage(t.getName() + " with boolean " + d);
    }

}

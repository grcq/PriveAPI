package cf.grcq.priveapi.command.defaults;

import cf.grcq.priveapi.command.Command;
import cf.grcq.priveapi.command.parameter.Param;
import cf.grcq.priveapi.tag.TagHandler;
import cf.grcq.priveapi.utils.Util;
import cf.grcq.priveapi.utils.VersionUtils;
import org.bukkit.entity.Player;

public class TagTestCommand {

    @Command(names = "tagtest")
    public static void tagTest(Player player, @Param(name = "name") String name) {
        TagHandler tagHandler = new TagHandler();
        tagHandler.setName(player, Util.format(name));
        tagHandler.update(player);

        player.sendMessage(VersionUtils.getNMSVersion());
    }

}

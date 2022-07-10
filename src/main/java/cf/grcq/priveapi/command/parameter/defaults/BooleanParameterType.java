package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class BooleanParameterType implements ParameterType<Boolean> {

    private final List<String> trueList = Lists.newArrayList("true", "yes", "on");
    private final List<String> falseList = Lists.newArrayList("false", "no", "off");

    @Override
    public Boolean transform(CommandSender sender, String source) {

        if (trueList.contains(source)) return true;

        if (falseList.contains(source)) return false;

        sender.sendMessage(Util.format("&cError: Invalid boolean. Please use true of false."));
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String source) {
        return Lists.newArrayList("true", "false");
    }
}

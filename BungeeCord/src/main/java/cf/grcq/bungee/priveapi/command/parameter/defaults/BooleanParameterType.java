package cf.grcq.bungee.priveapi.command.parameter.defaults;

import cf.grcq.bungee.priveapi.command.parameter.ParameterType;
import cf.grcq.bungee.priveapi.utils.Util;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.CommandSender;

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
}

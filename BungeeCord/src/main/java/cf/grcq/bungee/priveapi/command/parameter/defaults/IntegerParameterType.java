package cf.grcq.bungee.priveapi.command.parameter.defaults;

import cf.grcq.bungee.priveapi.command.parameter.ParameterType;
import cf.grcq.bungee.priveapi.utils.Util;
import net.md_5.bungee.api.CommandSender;

public class IntegerParameterType implements ParameterType<Integer> {

    @Override
    public Integer transform(CommandSender sender, String source) {

        try {
            return Integer.parseInt(source);
        } catch (Exception e) {
            sender.sendMessage(Util.format("&cError: '&e" + source + "&c' is not a valid integer."));
        }

        return null;
    }
}

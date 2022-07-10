package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import org.bukkit.command.CommandSender;

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

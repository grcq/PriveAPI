package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import org.bukkit.command.CommandSender;

public class StringParameterType implements ParameterType<String> {

    @Override
    public String transform(CommandSender sender, String source) {
        return source;
    }
}

package cf.grcq.bungee.priveapi.command.parameter.defaults;

import cf.grcq.bungee.priveapi.command.parameter.ParameterType;
import net.md_5.bungee.api.CommandSender;

public class StringParameterType implements ParameterType<String> {

    @Override
    public String transform(CommandSender sender, String source) {
        return source;
    }
}

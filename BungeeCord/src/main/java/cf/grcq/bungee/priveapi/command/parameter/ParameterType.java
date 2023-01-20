package cf.grcq.bungee.priveapi.command.parameter;

import net.md_5.bungee.api.CommandSender;

public interface ParameterType<T> {

    T transform(CommandSender sender, String source);

}

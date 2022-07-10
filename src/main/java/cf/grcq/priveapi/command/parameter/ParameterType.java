package cf.grcq.priveapi.command.parameter;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface ParameterType<T> {

    T transform(CommandSender sender, String source);

    default List<String> tabComplete(Player player, String source) {
        return new ArrayList<>();
    }

}

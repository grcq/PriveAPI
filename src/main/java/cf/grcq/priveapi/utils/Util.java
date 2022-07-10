package cf.grcq.priveapi.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

@UtilityClass
public class Util {

    public static String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}

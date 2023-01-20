package cf.grcq.bungee.priveapi.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class Util {

    public static String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> format(List<String> arr) {
        List<String> a = new ArrayList<>();
        for (String s : arr) {
            a.add(format(s));
        }

        Collections.reverse(a);
        return a;
    }

}

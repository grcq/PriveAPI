package cf.grcq.priveapi.utils;

import org.bukkit.Bukkit;

public class VersionUtils {

    public static String getNMSVersion() {
        String v = Bukkit.getServer().getClass().getPackage().getName();
        v = v.substring(v.lastIndexOf('.') + 1);

        return v;
    }

    public static String getServerVersion() {
        return Bukkit.getServer().getVersion();
    }

}

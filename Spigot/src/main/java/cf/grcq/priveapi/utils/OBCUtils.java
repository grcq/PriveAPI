package cf.grcq.priveapi.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class OBCUtils {

    @Nullable
    public static Class<?> getOBCClass(String name) {
        String version = VersionUtils.getNMSVersion();
        try {
            return Class.forName("org.bukkit.craftbukkit" + version + "." + name);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Could not find NMS Class '" + name + ".java' (NMS Version: " + version + ").");
            return null;
        }
    }

}

package cf.grcq.priveapi.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class NMSUtils {

    @Nullable
    public static Class<?> getNMSClass(String name) {
        String version = VersionUtils.getNMSVersion();
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Could not find NMS Class '" + name + ".java' (NMS Version: " + version + ").");
            return null;
        }
    }

}

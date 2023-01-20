package cf.grcq.bungee.priveapi.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class NMSUtils {

    @Nullable
    public static Class<?> getNMSClass(String name) {
        String version = VersionUtils.getNMSVersion();
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (Exception e) {
            ProxyServer.getInstance().getLogger().severe("Could not find NMS Class '" + name + ".java' (NMS Version: " + version + ").");
            return null;
        }
    }

}

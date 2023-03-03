package dev.grcq.v1_12_r1;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Tag {

    @Getter
    private static Map<Player, String> names = new HashMap<>();

    @SneakyThrows
    public static void update() {
        for (Map.Entry<Player, String> entry : names.entrySet()) {
            update(entry.getKey());
        }
    }

    @SneakyThrows
    public static void update(Player player) {
        String name = names.get(player);

        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        GameProfile profile = ep.getProfile();
        Field field = profile.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(profile, name);

        for (Player all : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) all).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep));
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep));
        }
    }

}

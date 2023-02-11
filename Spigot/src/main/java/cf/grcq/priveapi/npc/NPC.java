package cf.grcq.priveapi.npc;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.UUID;

import static cf.grcq.priveapi.utils.NMSUtils.getNMSClass;
import static cf.grcq.priveapi.utils.OBCUtils.getOBCClass;

public class NPC {

    private final GameProfile profile;

    @Getter
    private final Object entity;

    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    public NPC(@NotNull Location loc, @NotNull String name, @NotNull String skinName) {
        Object server = getOBCClass("CraftSerever").getMethod("getServer").invoke(Bukkit.getServer());
        Object world = getOBCClass("CraftWorld").getMethod("getHandle").invoke(loc.getWorld());

        profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", null);

        Constructor<?> entityConstructor = getNMSClass("EntityPlayer").getDeclaredConstructors()[0];
        Constructor<?> interactConstructor = getNMSClass("PlayerInteractManagere").getDeclaredConstructors()[0];

        entity = entityConstructor.newInstance(server, world, profile, interactConstructor.newInstance(server));
        entity.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class).invoke(entity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    public void show(Player player) {
        Object addPlayer = getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getField("ADD_PLAYER").get(null);
        Constructor<?> cons = getNMSClass("PackerPlayOutPlayerInfo").getConstructor(getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"));
    }

}

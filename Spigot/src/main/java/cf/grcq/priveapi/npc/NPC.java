package cf.grcq.priveapi.npc;

import cf.grcq.priveapi.utils.VersionUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
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
    public NPC(@NotNull Location loc, @NotNull String name, @NotNull String texture, @NotNull String signature) {
        if (name.length() > 16) throw new Exception("`name.length()` is greater than 16");

        Object server = getOBCClass("CraftServer").getMethod("getServer").invoke(Bukkit.getServer());
        Object world = getOBCClass("CraftWorld").getMethod("getHandle").invoke(loc.getWorld());

        profile = new GameProfile(UUID.randomUUID(), name);
        profile.getProperties().put("textures", new Property("textures", texture, signature));

        Constructor<?> entityConstructor = getNMSClass("EntityPlayer").getDeclaredConstructors()[0];
        Constructor<?> interactConstructor = getNMSClass("PlayerInteractManager").getDeclaredConstructors()[0];

        entity = entityConstructor.newInstance(server, world, profile, interactConstructor.newInstance(server));
        entity.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class).invoke(entity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    public void show(Player player) {
        Object addPlayer = getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getField("ADD_PLAYER").get(null);
        Constructor<?> infoCons = getNMSClass("PackerPlayOutPlayerInfo").getConstructor(getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"), Class.forName("[Lnet.minecraft.server." + VersionUtils.getNMSVersion() + ".EntityPlayer"));

        Object arr = Array.newInstance(getNMSClass("EntityPlayer"), 1);
        Array.set(arr, 0, this.entity);

        Object infoPacket = infoCons.newInstance(addPlayer, arr);
        this.sendPacket(player, infoPacket);

        Constructor<?> namedCons = getNMSClass("PacketPlayOutNamedEntitySpawn").getConstructor(getNMSClass("EntityHuman"));
        Object namedPacket = namedCons.newInstance(this.entity);
        this.sendPacket(player, namedPacket);

        Constructor<?> headRotationCons = getNMSClass("PacketPlayOutEntityHeadRotation").getConstructor(getNMSClass("Entity"), byte.class);
        float yaw = (float) this.entity.getClass().getField("yaw").get(this.entity);
        Object headRotationPacket = headRotationCons.newInstance(this.entity, (byte) (yaw * 256 / 360));

        this.sendPacket(player, headRotationPacket);
    }

    @SneakyThrows
    private void sendPacket(Player player, Object packet) {
        Object handle = player.getClass().getMethod("getHandle").invoke(player);
        Object conn = handle.getClass().getField("playerConnection").get(handle);

        conn.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(conn, packet);
    }

}

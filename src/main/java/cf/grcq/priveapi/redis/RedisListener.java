package cf.grcq.priveapi.redis;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

public interface RedisListener {

    @SneakyThrows
    default void receivedPacket(RedisPacket packet, JsonObject object) {
        Method[] methods = getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(PacketListener.class)) {
                PacketListener listener = method.getAnnotation(PacketListener.class);
                if (method.getParameterCount() != 1 || method.getParameters()[0].getType() != JsonObject.class) continue;

                if (packet.getAction().equalsIgnoreCase(listener.action())) {
                    method.invoke(this, object);
                }
            }
        }
    }

}

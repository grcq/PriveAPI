package cf.grcq.priveapi.rabbitmq;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RabbitMQHandler {

    public static RabbitMQHandler i;

    private final ConnectionFactory factory;

    private final Connection connection;
    private final Channel channel;
    private final String queue;

    private final List<Listener> listeners = new ArrayList<>();

    public RabbitMQHandler(String host, int port, String username, String password, String queue) {
        this(host, port, username, password, "/", queue);
    }

    @SneakyThrows
    public RabbitMQHandler(String host, int port, String username, String password, String vHost, String queue) {
        i = this;

        this.factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);

        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(vHost);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.queue = queue;

        channel.queueDeclare(queue, false, false, false, null);

        listen();
    }

    public void registerListener(Listener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    @SneakyThrows
    public void send(String queue, String action, JsonObject object) {
        object.addProperty("action", action);
        channel.basicPublish("", queue, false, null, object.toString().getBytes());
    }

    @SneakyThrows
    private void listen() {
        channel.basicConsume(queue, true, (tag, message) -> {
            try {
                JsonObject object = (JsonObject) new JsonParser().parse(new String(message.getBody(), StandardCharsets.UTF_8));

                for (Listener listener : i.listeners) {
                    if (!object.has("action")) {
                        Bukkit.getLogger().severe("[RabbitMQ] Failed to send parse packet.");
                        return;
                    }

                    for (Method method : listener.getClass().getDeclaredMethods()) {
                        if (method.isAnnotationPresent(PacketListener.class)) {
                            PacketListener anno = method.getAnnotation(PacketListener.class);

                            if (anno.action().equalsIgnoreCase(object.get("action").getAsString())) {
                                if (method.getParameterCount() != 1 || method.getParameters()[0].getType() != JsonObject.class) {
                                    Bukkit.getLogger().severe("[RabbitMQ] Packet listener method does not have the correct parameter.");
                                    return;
                                }

                                method.invoke(listener, object);
                            }
                        }
                    }
                }
            } catch (JsonParseException e) {
                Bukkit.getLogger().severe("[RabbitMQ] Failed to send parse packet.");
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }, tag -> {});
    }

    @SneakyThrows
    public void close() {
        channel.close();
        connection.close();
    }


}

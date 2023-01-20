package cf.grcq.bungee.priveapi.redis;

import cf.grcq.bungee.priveapi.PriveAPIB;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RedisHandler {

    private Map<String, RedisPacket> packets;
    private List<RedisListener> listeners;

    private final RedisCredentials credentials;
    private final String channel;

    private JedisPubSub pubSub;
    private Jedis jedis;

    private boolean logPackets;

    public RedisHandler(RedisCredentials credentials, String channel, boolean logPackets) {
        this.credentials = credentials;
        this.channel = channel;

        this.packets = new HashMap<>();
        this.listeners = new ArrayList<>();

        this.logPackets = logPackets;

        listen();
    }

    public RedisHandler(RedisCredentials credentials, String channel) {
        this(credentials, channel, true);
    }

    public RedisHandler(RedisCredentials credentials) {
        this(credentials, "default_channel");
    }

    public void registerListener(RedisListener... listeners) {
        this.listeners.addAll(Lists.newArrayList(listeners));
    }

    public void registerPacket(RedisPacket... packets) {
        for (RedisPacket packet : packets) {
            this.packets.put(packet.getAction(), packet);
        }
    }

    public void sendPacket(RedisPacket packet) {
        if (packet == null || packet.getAction() == null) {
            return;
        }

        String action = packet.getAction();

        if (packet.getMessage() == null) {
            sendStatus(Status.FAILED_PARSE, action, null);
            return;
        }

        JsonObject object = packet.getMessage();
        object.addProperty("action", packet.getAction());

        try {
            this.jedis.publish(channel, object.toString());
            sendStatus(Status.SUCCESS, action, packet.getClass());
        } catch (Exception e) {
            sendStatus(Status.FAILED, action, null);
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            this.pubSub = get();
            this.jedis = new Jedis(credentials.getAddress(), credentials.getPort());
            jedis.auth(credentials.getUsername(), credentials.getPassword());

            new Thread(() -> this.jedis.subscribe(this.pubSub, channel)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JedisPubSub get() {
        return new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase(RedisHandler.this.channel)) {
                    JsonObject object = new JsonParser().parse(message).getAsJsonObject();

                    if (RedisHandler.this.packets.containsKey(object.get("action").getAsString())) {
                        RedisPacket packet = RedisHandler.this.packets.get(object.get("action").getAsString());

                        for (RedisListener listener : RedisHandler.this.listeners) {
                            listener.receivedPacket(packet, object);
                        }
                    }
                }
            }
        };
    }

    public void sendStatus(Status status, String action, @Nullable Class<? extends RedisPacket> clazz) {
        if (!logPackets) return;
        Logger logger = PriveAPIB.getInstance().getLogger();

        switch (status) {
            case SUCCESS:
                logger.info(String.format("[Redis] Successfully sent packet with action ID of '%s' %s.", action, (clazz == null ? "" : "(Class: " + clazz.getSimpleName() + ".java)")));
                break;
            case FAILED:
                logger.severe(String.format("[Redis] Failed to send packet with action ID of '%s'.", action));
                break;
            case FAILED_PARSE:
                logger.severe(String.format("[Redis] Failed to parse packet with action ID of '%s'.", action));
                break;
            case FAILED_CONNECT:
                logger.warning(String.format("[Redis] Could not connect to Redis server."));
                break;
            default:
                break;
        }
    }

    public enum Status {
        SUCCESS, FAILED_PARSE, FAILED, FAILED_CONNECT
    }

}

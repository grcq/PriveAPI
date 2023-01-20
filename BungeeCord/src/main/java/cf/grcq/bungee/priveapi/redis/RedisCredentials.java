package cf.grcq.bungee.priveapi.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RedisCredentials {

    private final String address;
    private final int port;
    private final String username;
    private final String password;

    public RedisCredentials(String address) {
        this(address, "");
    }

    public RedisCredentials(String address, String username) {
        this(address, username, "");
    }

    public RedisCredentials(String address, String username, String password) {
        this(address, 6379, username, password);
    }

    public RedisCredentials(String address, int port) {
        this(address, port, "default");
    }

    public RedisCredentials(String address, int port, String username) {
        this(address, port, username, "");
    }

    public RedisCredentials(int port) {
        this(port, "default");
    }

    public RedisCredentials(int port, String username) {
        this(port, username, "");
    }

    public RedisCredentials(int port, String username, String password) {
        this("127.0.0.1", port, username, password);
    }

}

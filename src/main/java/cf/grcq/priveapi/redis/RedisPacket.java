package cf.grcq.priveapi.redis;

import com.google.gson.JsonObject;

public interface RedisPacket {

    String getAction();

    JsonObject getMessage();

}

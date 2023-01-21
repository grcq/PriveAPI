package cf.grcq.processor.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class JsonUtil {

    public static JsonPrimitive fix(final Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof String) {
            return new JsonPrimitive((String) o);
        } else if (o instanceof Number) {
            return new JsonPrimitive((Number) o);
        } else if (o instanceof Boolean) {
            return new JsonPrimitive((Boolean) o);
        }

        return null;
    }

}

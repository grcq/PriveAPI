package cf.grcq.processor.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Field;

public class JsonUtil {

    public static JsonPrimitive fix(final Field field, final Class<?> clazz) {
        try {
            Object o = field.get(clazz.getDeclaredConstructor().newInstance());
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
        } catch (Exception e) {
            return null;
        }

        return null;
    }

}

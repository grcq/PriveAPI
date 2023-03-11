package cf.grcq.priveapi.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

@UtilityClass
public class HttpUtils {

    @Nullable
    @SneakyThrows
    public static JsonObject getJson(@NotNull String urlStr) {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
            JsonObject object = new JsonObject();
            object.addProperty("error", true);
            object.addProperty("code", connection.getResponseCode());
            object.addProperty("message", "Could not receive the response from the current URL.");
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String res = reader.lines().collect(Collectors.joining("")).replace("\t", "").replace(" ", "").replace("  ", "");

        return (JsonObject) new JsonParser().parse(res);
    }

}

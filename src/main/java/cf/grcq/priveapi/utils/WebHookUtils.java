package cf.grcq.priveapi.utils;

import cf.grcq.priveapi.PriveAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

@UtilityClass
public class WebHookUtils {

    public static void sendWebhook(String url, String content, JsonObject object) {
        Bukkit.getScheduler().runTaskAsynchronously(PriveAPI.getInstance(), () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JsonArray array = new JsonArray();
                array.add(object);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("tts", false);
                if (!content.isEmpty()) jsonObject.addProperty("content", content);
                jsonObject.add("embeds", array);

                OutputStream stream = connection.getOutputStream();
                stream.write(jsonObject.toString().getBytes());
                stream.flush();
                stream.close();

                connection.getInputStream().close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}

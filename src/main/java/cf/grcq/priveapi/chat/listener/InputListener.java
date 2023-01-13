package cf.grcq.priveapi.chat.listener;

import cf.grcq.priveapi.PriveAPI;
import cf.grcq.priveapi.chat.input.Input;
import cf.grcq.priveapi.chat.output.Output;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InputListener implements Listener {

    private static final Map<UUID, Input> inputMap = new HashMap<>();
    private static final Map<Input, Long> lastedMap = new HashMap<>();

    public static void call(@NotNull Input input) {
        inputMap.put(input.getReceiver(), input);
        lastedMap.put(input, System.currentTimeMillis());

        if (input.getExpireIn() == -1 || input.getExpireIn() == Integer.MAX_VALUE) {
            Bukkit.getScheduler().runTaskLater(PriveAPI.getInstance(), () -> {
                inputMap.remove(input.getReceiver());
            }, (input.getExpireIn() * 20L));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        Input input = inputMap.get(uuid);
        if (input == null) return;

        input.getInput().accept(e);
        Output output = new Output(p, e.getMessage(), (System.currentTimeMillis() + lastedMap.get(input)), e.getHandlers());

        input.setOutput(output);

        inputMap.remove(uuid);
    }

}

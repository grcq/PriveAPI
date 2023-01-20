package cf.grcq.bungee.priveapi.chat.listener;

import cf.grcq.bungee.priveapi.PriveAPIB;
import cf.grcq.bungee.priveapi.chat.input.Input;
import cf.grcq.bungee.priveapi.chat.output.Output;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class InputListener implements Listener {

    private static final Map<UUID, Input> inputMap = new HashMap<>();
    private static final Map<Input, Long> lastedMap = new HashMap<>();

    public static void call(@NotNull Input input) {
        inputMap.put(input.getReceiver(), input);
        lastedMap.put(input, System.currentTimeMillis());

        if (input.getExpireIn() == -1 || input.getExpireIn() == Integer.MAX_VALUE) {
            ProxyServer.getInstance().getScheduler().schedule(PriveAPIB.getInstance(), () -> {
                inputMap.remove(input.getReceiver());
            }, input.getExpireIn(), TimeUnit.SECONDS);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent e) {
        ProxiedPlayer p = (ProxiedPlayer) e.getSender();
        UUID uuid = p.getUniqueId();

        Input input = inputMap.get(uuid);
        if (input == null) return;

        input.getInput().accept(e);
        Output output = new Output(p, e.getMessage(), (System.currentTimeMillis() + lastedMap.get(input)));

        input.setOutput(output);

        inputMap.remove(uuid);
    }

}

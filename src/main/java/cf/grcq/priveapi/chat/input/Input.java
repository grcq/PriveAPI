package cf.grcq.priveapi.chat.input;

import cf.grcq.priveapi.chat.output.Output;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.function.Consumer;

@Data
public class Input {

    private final UUID receiver;
    private final Consumer<AsyncPlayerChatEvent> input;
    private final int expireIn;

    private Output output;

    public Input(UUID receiver, Consumer<AsyncPlayerChatEvent> input, int expireIn) {
        this.receiver = receiver;
        this.input = input;
        this.expireIn = expireIn;
    }

}

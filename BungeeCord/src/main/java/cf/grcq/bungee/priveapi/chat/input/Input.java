package cf.grcq.bungee.priveapi.chat.input;

import cf.grcq.bungee.priveapi.chat.output.Output;
import lombok.Data;
import net.md_5.bungee.api.event.ChatEvent;

import java.util.UUID;
import java.util.function.Consumer;

@Data
public class Input {

    private final UUID receiver;
    private final Consumer<ChatEvent> input;
    private final int expireIn;

    private Output output;

    public Input(UUID receiver, Consumer<ChatEvent> input, int expireIn) {
        this.receiver = receiver;
        this.input = input;
        this.expireIn = expireIn;
    }

}

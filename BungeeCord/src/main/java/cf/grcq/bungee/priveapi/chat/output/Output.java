package cf.grcq.bungee.priveapi.chat.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Data
@AllArgsConstructor
public class Output {

    private final ProxiedPlayer sender;
    private final String message;
    private final long timeLasted;

}

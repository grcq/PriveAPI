package cf.grcq.priveapi.chat.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@Data
@AllArgsConstructor
public class Output {

    private final Player sender;
    private final String message;
    private final long timeLasted;
    private final HandlerList handlerList;

}

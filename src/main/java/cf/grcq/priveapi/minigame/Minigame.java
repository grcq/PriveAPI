package cf.grcq.priveapi.minigame;

import cf.grcq.priveapi.minigame.stats.Stats;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface Minigame {

    void onStart();

    void updateStats(Player player, Event event, Stats stats);

    void onEnd();

}

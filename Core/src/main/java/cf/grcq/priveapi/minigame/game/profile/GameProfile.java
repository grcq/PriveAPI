package cf.grcq.priveapi.minigame.game.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.OptionalInt;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GameProfile {

    private final UUID uuid;
    private final String username;

    private boolean alive;
    private boolean spectating;
    private boolean online;

    private int kills;
    private int deaths;
    private int killstreak;
    private OptionalInt collectedPoints;
    private OptionalInt collectedCoins;

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

}

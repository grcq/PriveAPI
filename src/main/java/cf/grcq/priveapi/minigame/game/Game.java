package cf.grcq.priveapi.minigame.game;

import cf.grcq.priveapi.minigame.game.profile.GameProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public abstract class Game {

    private boolean active;
    private boolean privateGame;
    private GameState gameState;

    public Game() {
        this(false, false, GameState.WAITING);
    }

    public Game(boolean privateGame) {
        this(false, privateGame, GameState.WAITING);
    }

    public Game(boolean privateGame, GameState state) {
        this(false, privateGame, state);
    }

    public Game(GameState state) {
        this(false, false, state);
    }

    abstract public String getId();
    abstract public String getName();
    abstract public List<GameProfile> getPlayers();
    abstract public int getMaxPlayers();
    abstract public World getMapWorld();

    @Nullable
    public GameProfile getGameProfile(UUID uuid) {

        String uuidString = uuid.toString();
        for (GameProfile profile : getPlayers()) {
            if (profile.getUuid().toString().equalsIgnoreCase(uuidString)) return profile;
        }

        return null;
    }

    @Nullable
    public GameProfile getGameProfile(String name) {
        for (GameProfile profile : getPlayers()) {
            if (profile.getUsername().equalsIgnoreCase(name)) return profile;
        }

        return null;
    }


}

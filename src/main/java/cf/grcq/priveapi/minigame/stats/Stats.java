package cf.grcq.priveapi.minigame.stats;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public abstract class Stats {

    private final UUID uuid;

    abstract public Map<String, Class<?>> getFields();

    abstract public void update();
}

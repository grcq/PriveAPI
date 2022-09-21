package cf.grcq.priveapi.minigame;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MinigameHandler {

    @Getter
    private static final List<Minigame> registeredMinigames = new ArrayList<>();

}

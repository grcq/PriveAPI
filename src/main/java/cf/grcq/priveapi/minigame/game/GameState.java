package cf.grcq.priveapi.minigame.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameState {

    WAITING("Waiting"), STARTING("Starting"), ACTIVE("Active"), END("End");

    private final String asString;

}

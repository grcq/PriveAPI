package cf.grcq.priveapi.gui.buttons;

import cf.grcq.priveapi.gui.Button;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public class PreviousPageButton extends Button {

    private final int newPage;

    @Override
    public String getName(Player player) {
        return null;
    }

    @Override
    public List<String> getLore(Player player) {
        return null;
    }

    @Override
    public Material getMaterial(Player player) {
        return null;
    }

    @Override
    public boolean cancelClick(Player player) {
        return false;
    }
}

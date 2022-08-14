package cf.grcq.priveapi.gui.buttons;

import cf.grcq.priveapi.gui.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GlassButton extends Button {

    @Override
    public String getName(Player player) {
        return " ";
    }

    @Override
    public List<String> getLore(Player player) {
        return new ArrayList<>();
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.STAINED_GLASS_PANE;
    }

    @Override
    public byte getDamageValue(Player player) {
        return 15;
    }

    @Override
    public boolean cancelClick(Player player) {
        return true;
    }
}

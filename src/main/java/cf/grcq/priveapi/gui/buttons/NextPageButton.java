package cf.grcq.priveapi.gui.buttons;

import cf.grcq.priveapi.gui.Button;
import cf.grcq.priveapi.utils.Util;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

@AllArgsConstructor
public class NextPageButton extends Button {

    private final int newPage;

    @Override
    public String getName(Player player) {
        return Util.format("&aPrevious Page");
    }

    @Override
    public List<String> getLore(Player player) {
        return Util.format(Lists.newArrayList("&7Click here to go to page " + newPage));
    }

    @Override
    public Material getMaterial(Player player) {
        return Material.ARROW;
    }

    @Override
    public boolean cancelClick(Player player) {
        return true;
    }

    @Override
    public void onClick(Player player, ClickType clickType, int slot) {

    }
}

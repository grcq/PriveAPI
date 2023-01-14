package cf.grcq.priveapi.gui.buttons;

import cf.grcq.priveapi.PriveAPI;
import cf.grcq.priveapi.gui.Button;
import cf.grcq.priveapi.gui.PaginationGUI;
import cf.grcq.priveapi.utils.Util;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class PreviousPageButton extends Button {

    private final PaginationGUI to;

    public PreviousPageButton(PaginationGUI to) {
        this.to = to;
        to.setPage(to.getPage() - 1);
    }

    @Override
    public String getName(Player player) {
        return Util.format("&aPrevious Page");
    }

    @Override
    public List<String> getLore(Player player) {
        return Util.format(Lists.newArrayList("&7Click here to go to page " + to.getPage()));
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
        player.closeInventory();
        Bukkit.getScheduler().runTask(PriveAPI.getInstance(), () -> to.openGUI(player));
    }
}

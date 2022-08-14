package cf.grcq.priveapi.gui.listener;

import cf.grcq.priveapi.gui.Button;
import cf.grcq.priveapi.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();

        if (GUI.openGUIs.containsKey(player)) {
            GUI gui = GUI.openGUIs.get(player);
            if (gui == null) return;

            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
            }

            Button button = gui.getButtons(player).get(e.getSlot());
            if (button == null) return;

            if (button.cancelClick(player)) e.setCancelled(true);
            button.onClick(player, e.getClick(), e.getSlot());
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        if (GUI.openGUIs.containsKey(player)) {
            GUI gui = GUI.openGUIs.get(player);
            if (gui == null) return;

            gui.onClose(player);
            GUI.openGUIs.remove(player);

            BukkitRunnable runnable = GUI.tasks.get(player);
            if (runnable != null) {
                runnable.cancel();
                GUI.tasks.remove(player);
            };
        }
    }

}

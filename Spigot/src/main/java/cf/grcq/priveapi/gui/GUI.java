package cf.grcq.priveapi.gui;

import cf.grcq.priveapi.PriveAPIS;
import cf.grcq.priveapi.utils.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public abstract class GUI {

    @Getter @Setter private boolean autoUpdate = false;
    @Getter @Setter private long autoUpdateTime = 15L;

    public static final Map<Player, GUI> openGUIs = new HashMap<>();
    public static final Map<Player, BukkitRunnable> tasks = new HashMap<>();

    abstract public String getTitle(Player player);

    abstract public Map<Integer, Button> getButtons(Player player);

    public int size() {
        return 9;
    }

    public void onOpen(Player player) {}

    public void onClose(Player player) {}

    public void openGUI(Player player) {
        Inventory inventory = createGUI(player);

        onOpen(player);
        openGUIs.put(player, this);

        update(player);
        player.openInventory(inventory);
    }

    private Inventory createGUI(Player player) {
        Inventory inventory = Bukkit.getServer().createInventory(null, size(), Util.format(getTitle(player)));

        for (Map.Entry<Integer, Button> entry : getButtons(player).entrySet()) {
            Button button = entry.getValue();
            ItemStack itemStack = button.createItem(player);

            inventory.setItem(entry.getKey(), itemStack);
        }

        return inventory;
    }

    private void update(Player player) {

        BukkitRunnable runnable = new BukkitRunnable() {

            @Override
            public void run() {

                if (isAutoUpdate()) {
                    player.getOpenInventory().getTopInventory().setContents(createGUI(player).getContents());
                }

            }

        };
        runnable.runTaskTimer(PriveAPIS.getInstance(), 10L, getAutoUpdateTime());
        tasks.put(player, runnable);

    }

}

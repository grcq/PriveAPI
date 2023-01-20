package cf.grcq.startup.priveapi;

import cf.grcq.priveapi.PriveAPIS;
import org.bukkit.plugin.java.JavaPlugin;

public class Spigot extends JavaPlugin {

    private PriveAPIS api;

    @Override
    public void onEnable() {
        api = new PriveAPIS();
        api.onEnable();
    }

    @Override
    public void onDisable() {
        api.onDisable();
    }
}

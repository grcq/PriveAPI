package cf.grcq.startup.priveapi;

import cf.grcq.priveapi.PriveAPIS;
import cf.grcq.priveapi.chat.listener.InputListener;
import cf.grcq.priveapi.gui.listener.GUIListener;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static org.bukkit.Bukkit.getServer;

public class Spigot extends JavaPlugin {

    private PriveAPIS api;

    @SneakyThrows
    @Override
    public void onEnable() {;
    }

    @Override
    public void onDisable() {
        this.saveDefaultConfig();

        api.onDisable();
    }
}

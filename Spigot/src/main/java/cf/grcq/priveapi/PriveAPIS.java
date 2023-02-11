package cf.grcq.priveapi;

import cf.grcq.priveapi.command.CommandHandler;
import cf.grcq.priveapi.gui.listener.GUIListener;
import cf.grcq.priveapi.language.LanguageHandler;
import cf.grcq.priveapi.chat.listener.InputListener;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PriveAPIS extends JavaPlugin {

    @Getter private static PriveAPIS instance;

    @SneakyThrows
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        instance = this;

        CommandHandler.init();

        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new InputListener(), this);

        getDataFolder().mkdirs();

        File file = new File(getDataFolder(), "data.yml");
        if (!file.exists()) {
            file.createNewFile();
        }

        CommandHandler.registerClass(this, getClass());
    }

    @Override
    public void onDisable() {
        this.saveDefaultConfig();

    }
}

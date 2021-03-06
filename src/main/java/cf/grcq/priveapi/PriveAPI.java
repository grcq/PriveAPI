package cf.grcq.priveapi;

import cf.grcq.priveapi.command.CommandHandler;
import cf.grcq.priveapi.language.LanguageHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PriveAPI extends JavaPlugin {

    @Getter private static PriveAPI instance;

    @Getter private LanguageHandler languageHandler;

    @SneakyThrows
    @Override
    public void onEnable() {
        instance = this;

        CommandHandler.init();

        getDataFolder().mkdirs();

        File file = new File(getDataFolder(), "data.yml");
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

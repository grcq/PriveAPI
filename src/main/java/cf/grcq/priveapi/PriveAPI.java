package cf.grcq.priveapi;

import cf.grcq.priveapi.command.CommandHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class PriveAPI extends JavaPlugin {

    @Getter private static PriveAPI instance;

    @Override
    public void onEnable() {
        instance = this;

        CommandHandler.registerAll(this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

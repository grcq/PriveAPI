package cf.grcq.bungee.priveapi;

import cf.grcq.bungee.priveapi.chat.listener.InputListener;
import cf.grcq.bungee.priveapi.command.CommandHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public final class PriveAPIB extends Plugin {

    @Getter
    private static PriveAPIB instance;

    @SneakyThrows
    @Override
    public void onEnable() {
        instance = this;

        CommandHandler.init();

        getProxy().getPluginManager().registerListener(this, new InputListener());

        getDataFolder().mkdirs();

        File file = new File(getDataFolder(), "data.yml");
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    @Override
    public void onDisable() {

    }
}

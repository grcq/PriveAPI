package cf.grcq.startup.priveapi;

import cf.grcq.bungee.priveapi.PriveAPIB;
import net.md_5.bungee.api.plugin.Plugin;

public final class Bungee extends Plugin {

    private PriveAPIB api;

    @Override
    public void onEnable() {
        api = new PriveAPIB();
        api.onEnable();
    }

    @Override
    public void onDisable() {
        api.onDisable();
    }
}

package cf.grcq.startup.priveapi;

import cf.grcq.bungee.priveapi.Test;
import cf.grcq.bungee.priveapi.TestData;
import cf.grcq.priveapi.PriveAPIS;
import org.bukkit.plugin.java.JavaPlugin;

public class Spigot extends JavaPlugin {

    private PriveAPIS api;

    @Override
    public void onEnable() {
        api = new PriveAPIS();
        api.onEnable();

        TestData.init(Test.class);

        Test test = new Test();
        try {
            TestData.save(test);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        api.onDisable();
    }
}

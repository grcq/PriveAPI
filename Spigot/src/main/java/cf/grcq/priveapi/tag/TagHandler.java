package cf.grcq.priveapi.tag;

import cf.grcq.priveapi.utils.VersionUtils;
import org.bukkit.entity.Player;

public class TagHandler {

    public void setName(Player player, String name) {
        switch (VersionUtils.getNMSVersion()) {
            case "v1_8_R3":
                dev.grcq.v1_8_r3.Tag.getNames().put(player, name);
                break;
            case "v1_12_R1":
                dev.grcq.v1_12_r1.Tag.getNames().put(player, name);
                break;
        }
    }

    public void update(Player player) {
        switch (VersionUtils.getNMSVersion()) {
            case "v1_8_R3":
                dev.grcq.v1_8_r3.Tag.update(player);
                break;
            case "v1_12_R1":
                dev.grcq.v1_12_r1.Tag.update(player);
                break;
        }
    }

    public void updateAll() {
        switch (VersionUtils.getNMSVersion()) {
            case "v1_8_R3":
                dev.grcq.v1_8_r3.Tag.update();
                break;
            case "v1_12_R3":
                dev.grcq.v1_12_r1.Tag.update();
                break;
        }
    }

}

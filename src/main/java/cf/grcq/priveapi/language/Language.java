package cf.grcq.priveapi.language;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public interface Language {

    String getName();

    String getShortName();

    File getFile();

    FileConfiguration getConfiguration();

}

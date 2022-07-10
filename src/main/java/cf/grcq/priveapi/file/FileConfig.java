package cf.grcq.priveapi.file;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class FileConfig {

    private final File file;
    @Getter
    private final FileConfiguration configuration;

    public FileConfig(File file) {
        this.file = file;
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public Object get(String s) {
        return configuration.get(s);
    }

    public Object get(String s, Object def) {
        return configuration.get(s, def);
    }

    public FileConfig set(String s, Object value) {
        configuration.set(s, value);
        return this;
    }

    @SneakyThrows
    public FileConfig save() {
        configuration.save(file);
        return this;
    }
}

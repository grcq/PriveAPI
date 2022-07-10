package cf.grcq.priveapi.language;

import cf.grcq.priveapi.PriveAPI;
import cf.grcq.priveapi.file.FileConfig;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class LanguageHandler {

    @Getter
    private final Map<UUID, Language> activeLanguage;

    private final List<Language> registeredLanguages;
    private final boolean useDatabase;
    private final Consumer<Player> databaseSaveMethod;

    public LanguageHandler(boolean useDatabase, Consumer<Player> databaseSaveMethod) {
        this.registeredLanguages = new ArrayList<>();
        this.useDatabase = useDatabase;
        this.databaseSaveMethod = databaseSaveMethod;

        this.activeLanguage = new HashMap<>();

        FileConfig fileConfig = new FileConfig(new File(PriveAPI.getInstance().getDataFolder(), "data.yml"));
        ConfigurationSection section = fileConfig.getConfiguration().getConfigurationSection("data");
        for (String value : section.getKeys(false)) {
            UUID uuid = UUID.fromString(value);

            Language language = parseLanguage((String) fileConfig.get("data." + uuid + ".language", "en_GB"));
            activeLanguage.put(uuid, language);
        }
    }

    private Language parseLanguage(String shortName) {
        for (Language language : registeredLanguages) {
            if (language.getShortName().equalsIgnoreCase(shortName)) return language;
        }
        return null;
    }

    public LanguageHandler(Consumer<Player> databaseSaveMethod) {
        this(true, databaseSaveMethod);
    }

    public LanguageHandler() {
        this(false, null);
    }

    public void registerLanguages(Language... languages) {
        registeredLanguages.addAll(Arrays.asList(languages));
    }

    public void unregisterLanguage(Language language) {
        registeredLanguages.remove(language);
    }

    public void save(Player player, Language language) {
        activeLanguage.put(player.getUniqueId(), language);
        if (!useDatabase) {
            FileConfig fileConfig = new FileConfig(new File(PriveAPI.getInstance().getDataFolder(), "data.yml"));
            fileConfig.set("data." + player.getUniqueId() + ".language", language.getShortName());

            fileConfig.save();
            return;
        }

        databaseSaveMethod.accept(player);
    }

}

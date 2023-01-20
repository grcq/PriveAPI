package cf.grcq.bungee.priveapi.language;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class LanguageHandler {

    @Getter
    private final Map<UUID, Language> activeLanguage;

    @Getter
    private static final List<Language> registeredLanguages = new ArrayList<>();
    private final Consumer<ProxiedPlayer> databaseSaveMethod;

    public LanguageHandler(Consumer<ProxiedPlayer> databaseSaveMethod) {
        this.databaseSaveMethod = databaseSaveMethod;

        this.activeLanguage = new HashMap<>();
    }

    @Nullable
    public static Language parseLanguage(String shortName) {
        for (Language language : registeredLanguages) {
            if (language.getShortName().equalsIgnoreCase(shortName)) return language;
        }
        return null;
    }

    public void registerLanguages(Language... languages) {
        registeredLanguages.addAll(Arrays.asList(languages));
    }

    public void unregisterLanguage(Language language) {
        registeredLanguages.remove(language);
    }

    public void save(ProxiedPlayer player, Language language) {
        activeLanguage.put(player.getUniqueId(), language);

        databaseSaveMethod.accept(player);
    }

    public Language getLanguage(ProxiedPlayer player) {
        return activeLanguage.get(player.getUniqueId());
    }

}

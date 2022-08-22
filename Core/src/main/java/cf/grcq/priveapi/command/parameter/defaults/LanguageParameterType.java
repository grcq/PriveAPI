package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.language.Language;
import cf.grcq.priveapi.language.LanguageHandler;
import cf.grcq.priveapi.utils.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LanguageParameterType implements ParameterType<Language> {

    @Override
    public Language transform(CommandSender sender, String source) {
        Language language = LanguageHandler.parseLanguage(source);
        if (language != null) {
            return language;
        }

        sender.sendMessage(Util.format("&cError: Language '&e" + source + "&c' was not found"));
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String source) {
        List<String> completion = new ArrayList<>();

        for (Language language : LanguageHandler.getRegisteredLanguages()) {
            completion.add(language.getName());
        }

        return completion;
    }
}

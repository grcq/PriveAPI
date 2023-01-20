package cf.grcq.bungee.priveapi.command.parameter.defaults;

import cf.grcq.bungee.priveapi.command.parameter.ParameterType;
import cf.grcq.bungee.priveapi.language.Language;
import cf.grcq.bungee.priveapi.language.LanguageHandler;
import cf.grcq.bungee.priveapi.utils.Util;
import net.md_5.bungee.api.CommandSender;

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
}

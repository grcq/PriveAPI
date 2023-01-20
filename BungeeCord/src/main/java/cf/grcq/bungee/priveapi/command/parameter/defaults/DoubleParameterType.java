package cf.grcq.bungee.priveapi.command.parameter.defaults;

import cf.grcq.bungee.priveapi.command.parameter.ParameterType;
import cf.grcq.bungee.priveapi.utils.Util;
import net.md_5.bungee.api.CommandSender;

public class DoubleParameterType implements ParameterType<Double> {

    @Override
    public Double transform(CommandSender sender, String source) {
        if (source.toLowerCase().contains("e")) {
            sender.sendMessage(Util.format("&cError: '&e" + source + "&c' is not a valid number."));
            return null;
        }

        try {
            double parsed = Double.parseDouble(source);

            if (Double.isNaN(parsed) || !Double.isFinite(parsed)) {
                sender.sendMessage(Util.format("&cError: '&e" + source + "&c' is not a valid number."));
                return null;
            }

            return parsed;
        } catch (NumberFormatException exception) {
            sender.sendMessage(Util.format("&cError: '&e" + source + "&c' is not a valid number."));
        }

        return null;
    }
}

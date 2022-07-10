package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import org.bukkit.command.CommandSender;

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

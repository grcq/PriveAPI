package cf.grcq.priveapi.command.parameter.defaults;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MaterialParameterType implements ParameterType<Material> {

    @Override
    public Material transform(CommandSender sender, String source) {
        Material material = Material.getMaterial(source);
        if (material != null) return material;

        sender.sendMessage(Util.format("&cError: World '&e" + source + "&c' does not exist."));
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String source) {
        List<String> arguments = new ArrayList<>();
        if (!source.isEmpty()) {
            for (Material material : Material.values()) {
                arguments.add(StringUtils.capitalize(material.name().toLowerCase()));
            }
        }

        return null;
    }
}

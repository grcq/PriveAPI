package cf.grcq.priveapi.command;

import cf.grcq.priveapi.command.parameter.Param;
import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class CommandNode {

    private final Command annotation;

    private final Method method;
    private final String name;
    private final String[] aliases;
    private final String permission;
    private final String description;

    public CommandNode(Method method) {
        this.annotation = method.getAnnotation(Command.class);

        this.method = method;
        this.permission = annotation.permission();
        this.name = annotation.names()[0];
        this.description = annotation.description();

        List<String> newArgs = Lists.newArrayList(annotation.names().clone());
        newArgs.remove(0);
        this.aliases = newArgs.toArray(new String[0]);
    }

    protected List<String> tabComplete(Player player, String[] args) {
        List<String> arguments = new ArrayList<>();

        ParameterType<?> parameterType = null;
        if (args.length <= (method.getParameterCount() - 1)) {
            parameterType = CommandHandler.getParameters().get(method.getParameterTypes()[args.length]);
        }

        if (parameterType != null) {
            String arg = args[(args.length - 1)];
            for (String s : parameterType.tabComplete(player, arg)) {
                if (StringUtils.startsWithIgnoreCase(s, arg)) {
                    arguments.add(s);
                }
            }
        }

        return arguments;
    }

    protected boolean canAccess(CommandSender sender) {
        if (!(sender instanceof Player)) return true;
        if (permission.isEmpty()) return true;

        Player p = (Player) sender;
        if (permission.equalsIgnoreCase("op")) return p.isOp();

        if (p.isOp()) return true;
        if (p.hasPermission("*") || p.hasPermission("*.*")) return true;

        return p.hasPermission(permission);
    }

    protected void invoke(CommandSender sender, String[] args) throws IllegalAccessException, InvocationTargetException {

        boolean playerOnly = method.getParameters()[0].getType() == Player.class;
        if (playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(Util.format(CommandHandler.getPlayerOnlyMessage()));
            return;
        }

        if (!canAccess(sender)) {
            sender.sendMessage(Util.format(CommandHandler.getNoPermissionMessage()));
            return;
        }

        boolean a = false;
        boolean b = false;

        List<Object> parameters = new ArrayList<>();
        parameters.add((playerOnly ? ((Player) sender).getPlayer() : sender));

        List<Parameter> methodParameters = Lists.newArrayList(method.getParameters().clone());
        methodParameters.remove(0);

        int i = 0;
        for (Parameter parameter : methodParameters) {
            if (!parameter.isAnnotationPresent(Param.class)) continue;
            if (a) break;

            Param param = parameter.getAnnotation(Param.class);
            if (param == null) continue;

            String s = (i < args.length ? args[i] : param.defaultValue());
            if (param.wildcard()) {
                a = true;
                s = toString(args, i);
            }

            if (s == null || s.isEmpty()) {
                break;
            }

            Object object = CommandHandler.transformParameter(sender, s, parameter.getType());
            if (object == null) {
                b = true;
                break;
            }

            parameters.add(object);
            i++;
        }

        if ((parameters.size() - 1) < methodParameters.size() && !b) {
            sender.sendMessage(Util.format(getUsage()));
            return;
        }

        if (b) return;

        Object[] parameterObjects = parameters.toArray(new Object[0]);
        method.invoke(null, parameterObjects);
    }

    protected String getUsage() {
        StringBuilder str = new StringBuilder("&cUsage: /" + name + " ");

        for (Parameter parameter : method.getParameters()) {
            Param param = parameter.getAnnotation(Param.class);
            if (param != null) {
                boolean required = param.defaultValue().isEmpty();

                str.append((required ? "<" : "["))
                        .append(param.name())
                        .append((required ? ">" : "]"))
                        .append(" ");
            }
        }

        return str.toString();
    }

    private static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int arg = start; arg < args.length; arg++) {
            stringBuilder.append(args[arg]).append(" ");
        }

        return (stringBuilder.toString().trim());
    }

}

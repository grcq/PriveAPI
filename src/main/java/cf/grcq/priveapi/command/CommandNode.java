package cf.grcq.priveapi.command;

import cf.grcq.priveapi.command.parameter.Param;
import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.utils.Util;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Data
public class CommandNode {

    private final JavaPlugin plugin;
    private final Command annotation;

    private final Method method;
    private final String name;
    private final String[] aliases;
    private final String permission;
    private final String description;
    private final Map<String, CommandNode> children;

    public CommandNode(JavaPlugin plugin, Method method, Map<String, CommandNode> children) {
        this.plugin = plugin;
        this.annotation = method.getAnnotation(Command.class);

        this.method = method;
        this.permission = annotation.permission();
        this.name = annotation.names()[0];
        this.description = annotation.description();
        this.children = children;

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

        for (String child : children.keySet()) {
            String[] split = child.split(" ");
            if (split.length <= args.length) {
                String arg = split[args.length - 1];
                arguments.add(arg);
            }
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

        CommandNode child = null;
        List<String> argsList = Lists.newArrayList(args);
        for (int i = argsList.size(); i > 0; i--) {
            String[] c = argsList.toArray(new String[0]);

            CommandNode childFound = children.get(this.name + " " + String.join(" ", c));
            if (childFound != null) {
                child = childFound;
                break;
            }

            argsList.remove((i - 1));
        }

        if (child != null) {
            List<String> newArgs = Lists.newArrayList(args);
            newArgs.removeAll(
                    Lists.newArrayList(child.getName().replace(this.name + " ", "").split(" "))
            );

            child.invoke(sender, newArgs.toArray(new String[0]));
            return;
        }

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

        if (b) return;

        if ((annotation.sendUsage() && method.getParameterCount() <= 1) ||
                ((parameters.size() - 1) < methodParameters.size())) {
            sender.sendMessage(Util.format(getUsage()));
            return;
        }

        if (((parameters.size() - 1) > methodParameters.size() && !a)) {
            sender.sendMessage(Util.format(getUsage()));
            return;
        }

        Object[] parameterObjects = parameters.toArray(new Object[0]);
        if (annotation.async()) Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                method.invoke(null, parameterObjects);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        else method.invoke(null, parameterObjects);
    }

    protected String getUsage() {
        String usage;

        if (children.isEmpty()) {
            String a = CommandHandler.getUsageMessage();

            StringBuilder params = new StringBuilder();
            for (Parameter parameter : method.getParameters()) {
                Param param = parameter.getAnnotation(Param.class);
                if (param != null) {
                    boolean required = param.defaultValue().isEmpty();

                    params.append((required ? "<" : "["))
                            .append(param.name())
                            .append((required ? ">" : "]"))
                            .append(" ");
                }
            }

            usage = a.replace("{command}", this.name).replace("{arguments}", params.toString());
        } else {
            StringBuilder str = new StringBuilder();

            if (children.size() == 1 && method.getParameterCount() < 2) {
                for (Map.Entry<String, CommandNode> child : children.entrySet()) {
                    String a = CommandHandler.getUsageMessage();

                    List<String> childParameter = Lists.newArrayList(child.getKey().split(" "));
                    childParameter.remove(0);
                    String childParam = String.join(" ", childParameter.toArray(new String[0])) + " ";

                    StringBuilder params = new StringBuilder();
                    params.append(childParam);
                    for (Parameter parameter : child.getValue().getMethod().getParameters()) {
                        Param param = parameter.getAnnotation(Param.class);
                        if (param != null) {
                            boolean required = param.defaultValue().isEmpty();

                            params.append((required ? "<" : "["))
                                    .append(param.name())
                                    .append((required ? ">" : "]"))
                                    .append(" ");
                        }
                    }

                    str = new StringBuilder(a.replace("{command}", this.name).replace("{arguments}", params.toString()));
                }
            } else {
                str.append(CommandHandler.getUsageMessageListLine())
                .append("\n");

                int i = 0;
                for (Map.Entry<String, CommandNode> child : children.entrySet()) {
                    i++;

                    String name = child.getKey();
                    CommandNode childNode = child.getValue();

                    List<String> childParameter = Lists.newArrayList(name.split(" "));
                    childParameter.remove(0);
                    String childParam = String.join(" ", childParameter.toArray(new String[0])) + " ";

                    String a = CommandHandler.getUsageMessageList();
                    StringBuilder params = new StringBuilder();
                    params.append(childParam);

                    for (Parameter parameter : childNode.getMethod().getParameters()) {
                        Param param = parameter.getAnnotation(Param.class);
                        if (param != null) {
                            boolean required = param.defaultValue().isEmpty();

                            params.append((required ? "<" : "["))
                                    .append(param.name())
                                    .append((required ? ">" : "]"))
                                    .append(" ");
                        }
                    }

                    str.append(a.replace("{command}", this.name).replace("{arguments}", params.toString()));

                    if (i < children.size()) {
                        str.append("\n");
                    }
                }

                if (CommandHandler.isDoubleUsageMessageLine()) {
                    str.append("\n")
                            .append(CommandHandler.getUsageMessageListLine());
                }
            }
            usage = str.toString();
        }

        return usage;
    }

    private static String toString(String[] args, int start) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int arg = start; arg < args.length; arg++) {
            stringBuilder.append(args[arg]).append(" ");
        }

        return (stringBuilder.toString().trim());
    }

}

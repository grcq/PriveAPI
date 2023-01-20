package cf.grcq.bungee.priveapi.command;

import cf.grcq.bungee.priveapi.command.flag.Flag;
import cf.grcq.bungee.priveapi.command.parameter.Param;
import cf.grcq.bungee.priveapi.command.parameter.ParameterType;
import cf.grcq.bungee.priveapi.utils.Util;
import com.google.common.collect.Lists;
import com.sun.tools.javac.util.StringUtils;
import lombok.Data;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
public class CommandNode {

    private final Plugin plugin;
    private final Command annotation;

    private final Method method;
    private final String name;
    private final String[] aliases;
    private final String permission;
    private final String description;
    private final Map<String, CommandNode> children;

    public CommandNode(Plugin plugin, Method method, Map<String, CommandNode> children) {
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

    private List<CommandNode> getChild(String[] args, boolean full) {
        List<CommandNode> possibleChildren = new ArrayList<>();
        List<String> argsList = Lists.newArrayList(args);

        if (full) {
            for (int i = argsList.size(); i > 0; i--) {
                String[] c = argsList.toArray(new String[0]);

                CommandNode childFound = children.get(this.name + " " + String.join(" ", c));
                if (childFound != null) {
                    possibleChildren.add(childFound);
                    break;
                }

                argsList.remove((i - 1));
            }
        } else {
            for (Map.Entry<String, CommandNode> entry : children.entrySet()) {
                if (entry.getKey().startsWith(String.join(" ", args))) {
                    possibleChildren.add(entry.getValue());
                }
            }
        }

        return possibleChildren;
    }

    protected boolean canAccess(CommandSender sender) {
        if (!(sender instanceof ProxiedPlayer)) return true;
        if (permission.isEmpty()) return true;

        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (p.hasPermission("*") || p.hasPermission("*.*")) return true;

        return p.hasPermission(permission);
    }

    protected void invoke(CommandSender sender, String[] args) throws IllegalAccessException, InvocationTargetException {

        boolean playerOnly = method.getParameters()[0].getType() == ProxiedPlayer.class;
        if (playerOnly && !(sender instanceof ProxiedPlayer)) {
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
        parameters.add((playerOnly ? ((ProxiedPlayer) sender) : sender));

        List<Parameter> methodParameters = Lists.newArrayList(method.getParameters().clone());
        methodParameters.remove(0);

        List<CommandNode> possibleChildren = getChild(args, true);
        if (!possibleChildren.isEmpty()) {
            CommandNode child = possibleChildren.get(0);

            List<String> newArgs = Lists.newArrayList(args);
            newArgs.removeAll(
                    Lists.newArrayList(child.getName().replace(this.name + " ", "").split(" "))
            );

            child.invoke(sender, newArgs.toArray(new String[0]));
            return;
        }

        int flagsFound = 0;
        int flagsUsed = 0;
        int i = 0;
        for (Parameter parameter : methodParameters) {
            if (a) break;

            if (parameter.isAnnotationPresent(Param.class)) {
                Param param = parameter.getAnnotation(Param.class);
                if (param == null) continue;

                // Might come later.
                /*if (parameter.isAnnotationPresent(Flag.class)) {
                    Flag flag = parameter.getAnnotation(Flag.class);
                    if (flag == null) continue;

                    for (String s : args) {
                        System.out.println(s + " - ARG NUMBER " + i + " - FLAGS USED NUMBER " + (i - flagsUsed)); // Debug
                    }

                    String s = (i + flagsUsed < args.length ? args[i + flagsUsed] : param.defaultValue());
                    if (s.equalsIgnoreCase("-" + flag.name()))  {
                        if (args.length <= i + flagsUsed) {
                            System.out.println("ok"); // Debug
                            parameters.add(null);
                            continue;
                        }

                        String s_ = args[i + 1];

                        Object object = CommandHandler.transformParameter(sender, s_, parameter.getType());
                        System.out.println(object); // Debug
                        if (object == null) {
                            b = true;
                            break;
                        }

                        flagsUsed++;
                        parameters.add(object);
                    } else {
                        parameters.add(null);
                    }
                } else {*/
                String s = (i - flagsFound < args.length - flagsUsed ? args[i - flagsFound + flagsUsed] : param.defaultValue());

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
                //}
            } else if (parameter.isAnnotationPresent(Flag.class)) {
                Flag flag = parameter.getAnnotation(Flag.class);
                if (flag == null) continue;

                List<String> fullArguments = Arrays.asList(args);
                flagsFound += 1;

                boolean found = false;
                for (String flagName : flag.names()) {
                    if (i < fullArguments.size()) {
                        if (fullArguments.get(i).equalsIgnoreCase("-" + flagName)) {
                            found = true;
                            flagsUsed += 1;
                            break;
                        }
                    }
                }

                parameters.add(found);
            } else {
                throw new RuntimeException("Parameter does not have @Param or @Flag annotation.");
            }

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
        if (annotation.async()) ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
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
                Flag flag = parameter.getAnnotation(Flag.class);
                if (flag != null) {
                    params.append("[").append("-").append(flag.names()[0])
                            .append("] ");
                }

                if (param != null) {
                    boolean required = param.defaultValue().isEmpty();

                    if (flag == null) {
                        params.append((required ? "<" : "["))
                                .append(param.name())
                                .append((required ? "> " : "] "));
                    } else {
                        params.append("[").append(param.name()).append("] ");
                    }
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
                        Flag flag = parameter.getAnnotation(Flag.class);
                        if (flag != null) {
                            params.append("[").append("-").append(flag.names()[0])
                                    .append("] ");
                        }

                        if (param != null) {
                            boolean required = param.defaultValue().isEmpty();

                            if (flag == null) {
                                params.append((required ? "<" : "["))
                                        .append(param.name())
                                        .append((required ? "> " : "] "));
                            } else {
                                params.append("[").append(param.name()).append("] ");
                            }
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
                        Flag flag = parameter.getAnnotation(Flag.class);
                        if (flag != null) {
                            params.append("[").append("-").append(flag.names()[0])
                                    .append("] ");
                        }

                        if (param != null) {
                            boolean required = param.defaultValue().isEmpty();

                            if (flag == null) {
                                params.append((required ? "<" : "["))
                                        .append(param.name())
                                        .append((required ? "> " : "] "));
                            } else {
                                params.append("[").append(param.name()).append("] ");
                            }
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

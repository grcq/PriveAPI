package cf.grcq.priveapi.command;

import cf.grcq.priveapi.PriveAPI;
import cf.grcq.priveapi.command.defaults.CommandInfoCommand;
import cf.grcq.priveapi.command.defaults.TestCommands;
import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.command.parameter.defaults.*;
import cf.grcq.priveapi.utils.ClassUtils;
import com.google.common.base.Preconditions;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {

    @Getter(value = AccessLevel.PROTECTED)
    private static List<CommandNode> commands;
    @Getter(value = AccessLevel.PROTECTED)
    private static Map<Class<?>, ParameterType<?>> parameters;

    @Getter @Setter private static String noPermissionMessage;
    @Getter @Setter private static String playerOnlyMessage;
    @Getter @Setter private static String usageMessage;
    @Getter @Setter private static String usageMessageList;
    @Getter @Setter private static String usageMessageListLine;
    @Getter @Setter private static boolean doubleUsageMessageLine;

    private static CommandMap map;
    private static boolean initalized = false;

    public static void registerParameter(Class<?> clazz, ParameterType<?> parameterType) {
        parameters.put(clazz, parameterType);
    }

    public static void registerAll(JavaPlugin plugin) {
        for (Class<?> clazz : ClassUtils.getClassesInPackage(plugin.getClass(), plugin.getClass().getPackage().getName())) {
            registerClass(plugin, clazz);
        }
    }

    public static void registerClass(JavaPlugin plugin, Class<?> clazz) {

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                Command command = method.getAnnotation(Command.class);

                if (!command.names()[0].contains(" ")) {
                    Map<String, CommandNode> children = new HashMap<>();

                    for (Method child : clazz.getMethods()) {
                        if (child.isAnnotationPresent(Command.class)) {
                            Command childCommand = child.getAnnotation(Command.class);

                            if (childCommand.names()[0].contains(" ")) {
                                String main = command.names()[0];

                                if (main.equalsIgnoreCase(childCommand.names()[0].split(" ")[0])) {
                                    CommandNode childNode = new CommandNode(plugin, child, new HashMap<>());
                                    children.put(childCommand.names()[0], childNode);
                                }
                            }
                        }
                    }

                    registerMethod(plugin, method, children);
                }
            }
        }

    }

    private static void registerMethod(JavaPlugin plugin, Method method, Map<String, CommandNode> children) {
        CommandNode commandNode = new CommandNode(plugin, method, children);
        BukkitCommand command = new BukkitCommand(commandNode.getAnnotation(), commandNode);

        map.register(plugin.getDescription().getName().toLowerCase(), command);
        commands.add(commandNode);
    }

    public static void unregisterCommand(String command) {
        map.getCommand(command).unregister(map);
    }

    @SneakyThrows
    private static void updateMap() {
        if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();

            Field commandMap = pluginManager.getClass().getDeclaredField("commandMap");
            commandMap.setAccessible(true);

            map = (CommandMap) commandMap.get(pluginManager);
        }
    }

    @Nullable
    protected static Object transformParameter(CommandSender sender, String parameter, Class<?> transformTo) {
        if (transformTo == String.class) {
            return parameter;
        }

        ParameterType<?> type = parameters.get(transformTo);
        if (type == null) throw new IllegalArgumentException("Unknown parameter type for class '" + transformTo.getSimpleName() + ".java'. Forgot to register?");

        return type.transform(sender, parameter);
    }

    @Nullable
    public static CommandNode find(String command, JavaPlugin plugin) {
        CommandNode foundNode = null;

        for (CommandNode node : getCommands()) {
            if (node.getName().equalsIgnoreCase(command) && pluginEqual(node.getPlugin(), plugin)) {
                foundNode = node;
                break;
            }
        }

        return foundNode;
    }

    @Nullable
    public static CommandNode find(String command) {
        CommandNode foundNode = null;

        for (CommandNode node : getCommands()) {
            if (node.getName().equalsIgnoreCase(command)) {
                foundNode = node;
                break;
            }
        }

        return foundNode;
    }

    private static boolean pluginEqual(JavaPlugin plugin1, JavaPlugin plugin2) {
        return plugin1.getDescription().getName().equalsIgnoreCase(plugin2.getDescription().getName());
    }

    @SneakyThrows
    public static void init() {
        Preconditions.checkState(!initalized);
        initalized = true;

        commands = new ArrayList<>();
        parameters = new HashMap<>();

        updateMap();

        noPermissionMessage = "&cNo permission!";
        playerOnlyMessage = "&cPlayers only!";
        usageMessage = "&cUsage: /{command} {arguments}";
        usageMessageList = "&6/{command} &f{arguments}";
        usageMessageListLine = "&7&m-------------------------";
        doubleUsageMessageLine = true;


        registerParameter(String.class, new StringParameterType());
        registerParameter(Player.class, new PlayerParameterType());
        registerParameter(OfflinePlayer.class, new OfflinePlayerParameterType());
        registerParameter(Boolean.class, new BooleanParameterType());
        registerParameter(boolean.class, new BooleanParameterType());
        registerParameter(Integer.class, new IntegerParameterType());
        registerParameter(int.class, new IntegerParameterType());
        registerParameter(Float.class, new FloatParameterType());
        registerParameter(float.class, new FloatParameterType());
        registerParameter(Double.class, new DoubleParameterType());
        registerParameter(double.class, new DoubleParameterType());
        registerParameter(World.class, new WorldParameterType());
        registerParameter(Material.class, new MaterialParameterType());

        registerClass(PriveAPI.getInstance(), CommandInfoCommand.class);
        registerClass(PriveAPI.getInstance(), TestCommands.class);
    }

}

package cf.grcq.priveapi.command;

import cf.grcq.priveapi.command.parameter.ParameterType;
import cf.grcq.priveapi.command.parameter.defaults.*;
import cf.grcq.priveapi.utils.ClassUtils;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler {

    @Getter(value = AccessLevel.PROTECTED)
    private static final List<CommandNode> commands;
    @Getter(value = AccessLevel.PROTECTED)
    private static final Map<Class<?>, ParameterType<?>> parameters;

    @Getter
    @Setter
    private static String noPermissionMessage;
    @Getter
    @Setter
    private static String playerOnlyMessage;

    private static CommandMap map;

    public static void registerParameter(Class<?> clazz, ParameterType<?> parameterType) {
        parameters.put(clazz, parameterType);
    }

    public static void registerAll(JavaPlugin plugin) {
        for (Class<?> clazz : ClassUtils.getClassesInPackage(plugin, plugin.getClass().getPackage().getName())) {
            registerClass(plugin, clazz);
        }
    }

    public static void registerClass(JavaPlugin plugin, Class<?> clazz) {

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                registerMethod(plugin, method);
            }
        }

    }

    private static void registerMethod(JavaPlugin plugin, Method method) {
        CommandNode commandNode = new CommandNode(method);
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

            Field field = pluginManager.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);

            map = (CommandMap) field.get(pluginManager);
        }
    }

    protected static Object transformParameter(CommandSender sender, String parameter, Class<?> transformTo) {
        if (transformTo == String.class) {
            return parameter;
        }

        ParameterType<?> type = parameters.get(transformTo);
        if (type == null) throw new IllegalArgumentException("Unknown parameter type for class '" + transformTo.getSimpleName() + ".java'. Forgot to register?");

        return type.transform(sender, parameter);
    }

    static {
        commands = new ArrayList<>();
        parameters = new HashMap<>();

        updateMap();

        noPermissionMessage = "&cNo permission!";
        playerOnlyMessage = "&cPlayers only!";

        registerParameter(String.class, new StringParameterType());
        registerParameter(Player.class, new PlayerParameterType());
        registerParameter(OfflinePlayer.class, new OfflinePlayerParameterType());
        registerParameter(Boolean.class, new BooleanParameterType());
        registerParameter(Integer.class, new IntegerParameterType());
        registerParameter(Float.class, new FloatParameterType());
        registerParameter(Double.class, new DoubleParameterType());
    }

}

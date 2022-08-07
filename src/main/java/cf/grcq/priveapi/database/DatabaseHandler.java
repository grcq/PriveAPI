package cf.grcq.priveapi.database;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class DatabaseHandler {

    private static DatabaseHandler instance;

    @Getter
    private Connection connection;
    private final Map<Class<?>, List<Field>> classFields = new HashMap<>();

    private static String convertToSQL(Field field) {
        Class<?> clazz = field.getDeclaringClass();
        switch (clazz.getSimpleName()) {
            case "int":
            case "Integer":
                return "INT";
            case "long":
            case "Long":
                return "BIGINT";
            case "double":
            case "Double":
            case "float":
            case "Float":
                return "DOUBLE";
            case "boolean":
            case "Boolean":
                return "BOOLEAN";
            case "UUID":
                return "VARCHAR(36)";
            default:
                return "VARCHAR(200)";
        }
    }

    @SneakyThrows
    public static void init(JavaPlugin plugin) {
        instance = new DatabaseHandler();

        for (Class<?> clazz : new ArrayList<Class<?>>()) {
            if (clazz.isAnnotationPresent(Database.class)) {
                Database database = clazz.getAnnotation(Database.class);

                List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
                List<String> fieldNames = new ArrayList<>();
                for (Field field : fields) {
                    fieldNames.add(field.getName() + " " + convertToSQL(field));
                }

                instance.connect();
                instance.update("CREATE TABLE IF NOT EXISTS " + database.table() + " (" +
                        String.join(", ", fieldNames.toArray(new String[0])) +
                        ");");

                instance.connection.close();

                instance.classFields.put(clazz, fields);
            }
        }
    }

    @SneakyThrows
    public ResultSet execute(String sql) {
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps.executeQuery();
    }

    @SneakyThrows
    public void update(String sql) {
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.executeUpdate();
    }

    @SneakyThrows
    public void connect() {
        String uri = "jdbc:mysql://?useSSL=true";
        connection = DriverManager.getConnection(uri, "username", "password");
    }

}

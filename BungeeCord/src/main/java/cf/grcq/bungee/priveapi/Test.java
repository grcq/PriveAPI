package cf.grcq.bungee.priveapi;

import cf.grcq.processor.annotations.Database;
import cf.grcq.processor.database.Type;

@Database(table = "test", username = "", password = "", address = "", type = Type.MYSQL)
public class Test {

    private String testing;

}

package cf.grcq.bungee.priveapi;

import cf.grcq.processor.annotations.Database;
import cf.grcq.processor.database.Type;

@Database(table = "test", type = Type.MONGODB)
public class Test {

    private String testing;

}

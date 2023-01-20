package cf.grcq.bungee.priveapi.jda;

import net.dv8tion.jda.api.Permission;

public @interface JDACommand {

    String[] names();
    String description() default "";

    CommandType type() default CommandType.SLASH_COMMAND;

    Permission[] permissions() default {};

    boolean async() default false;
}

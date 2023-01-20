package cf.grcq.priveapi.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    String[] names();
    String permission() default "";
    String description() default "";

    boolean hidden() default false;
    boolean async() default false;
    boolean sendUsage() default false;
}

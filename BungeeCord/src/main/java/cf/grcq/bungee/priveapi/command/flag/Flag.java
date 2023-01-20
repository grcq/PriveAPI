package cf.grcq.bungee.priveapi.command.flag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {

    Pattern FLAG_PATTERN = Pattern.compile("(-)([a-zA-Z])([\\w]*)?");

    String[] names();

    boolean defaultValue() default false;

}

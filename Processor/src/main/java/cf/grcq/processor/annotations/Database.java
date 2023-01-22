package cf.grcq.processor.annotations;

import cf.grcq.processor.database.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Database {

    String table();

    Type type();

    String username();
    String password();
    String address();

    String database() default "";

    boolean srv() default false;

}

package cf.grcq.priveapi.utils;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class MethodUtils {

    public static Set<Method> getMethods(Class<?> clazz, Annotation... annotations) {
        Set<Method> methods = new HashSet<>();

        for (Method method : clazz.getMethods()) {
            boolean has = true;
            for (Annotation annotation : annotations) {
                if (!method.isAnnotationPresent(annotation.annotationType())) {
                    has = false;
                    break;
                }
            }

            if (has) {
                methods.add(method);
            }
        }

        return methods;
    }

}

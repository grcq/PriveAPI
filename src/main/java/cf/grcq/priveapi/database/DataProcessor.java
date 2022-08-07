package cf.grcq.priveapi.database;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DataProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Database.class)) {
            JavaFileObject object = null;
            PackageElement packageElement = (PackageElement) element.getEnclosedElements();

            try {
                object = processingEnv.getFiler().createSourceFile(element.getSimpleName() + "Test");
                if (element.getKind().isClass()) {
                    for (Element enclosed : element.getEnclosedElements()) {
                        if (enclosed.getKind().isField() && (enclosed.getModifiers().contains(Modifier.PUBLIC) | enclosed.getModifiers().contains(Modifier.PROTECTED))) {
                            BufferedWriter writer = new BufferedWriter(object.openWriter());
                            writer.append("package ");
                            writer.append(packageElement.getQualifiedName().toString());
                            writer.append(";");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Collections.singletonList("cf.grcq.priveapi.database.Database"));
    }
}

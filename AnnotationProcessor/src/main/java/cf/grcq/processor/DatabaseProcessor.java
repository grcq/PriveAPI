package cf.grcq.processor;

import cf.grcq.processor.annotations.Database;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("cf.grcq.processor.annotations.Database")
@AutoService(Processor.class)
public class DatabaseProcessor extends AbstractProcessor {

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
                            PrintWriter writer = new PrintWriter(object.openWriter());
                            writer.print("package ");
                            writer.print(packageElement.getQualifiedName().toString());
                            writer.println(";");
                            writer.println();

                            writer.print("public class ");
                            writer.print(element.getSimpleName());
                            writer.println("Test {");
                            writer.println();

                            writer.println("     public void hi() {}");
                            writer.println("}");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}

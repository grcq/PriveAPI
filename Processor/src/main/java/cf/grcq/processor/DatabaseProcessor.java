package cf.grcq.processor;

import cf.grcq.processor.annotations.Database;
import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("cf.grcq.processor.annotations.Database")
public class DatabaseProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Element el : roundEnv.getElementsAnnotatedWith(Database.class)) {
            List<Element> fields = el.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.FIELD).collect(Collectors.toList());

            String packageName = elementUtils.getPackageOf(el).getQualifiedName().toString();
            String className = el.getSimpleName().toString();
            String name = String.format("%sData", el.getSimpleName().toString());
            String classVariableName = className.substring(0, 1).toUpperCase() + className.substring(2);

            Database db = el.getAnnotation(Database.class);

            try {
                generateClass(db, packageName, className, classVariableName, name, fields, el);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write.");
            }
        }

        return true;
    }

    private void generateClass(Database db, String packageName, String className, String classVariableName, String name, List<Element> fields, Element typeElement) throws IOException {
        ClassName type = ClassName.get(packageName, name);

        TypeName target = TypeName.get(typeElement.asType());

        MethodSpec saveMethod = generateSaveMethod(fields, target, name, db);
        MethodSpec initMethod = generateInitMethod(fields, target, name, db);

        TypeSpec builder = TypeSpec.classBuilder(type.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(initMethod)
                .addMethod(saveMethod)
                .addField(FieldSpec.builder(MongoClient.class, "client", Modifier.PRIVATE, Modifier.STATIC).build())
                .addField(FieldSpec.builder(Gson.class, "gson", Modifier.PRIVATE, Modifier.STATIC).build())
                .build();

        JavaFile file = JavaFile.builder(type.packageName(), builder.toBuilder().build()).build();

        file.writeTo(filer);
    }

    private MethodSpec generateInitMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(Class.class, "databaseClass").build())
                .addCode("""
                        client = com.mongodb.client.MongoClients.create(((cf.grcq.processor.annotations.Database) databaseClass.getAnnotation(cf.grcq.processor.annotations.Database.class)).type().getUri());
                        gson = new com.google.gson.GsonBuilder().setLongSerializationPolicy(com.google.gson.LongSerializationPolicy.STRING).create();
                        """
                )
                .returns(TypeName.VOID);

        return builder.build();
    }

    private MethodSpec generateSaveMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(typeName, "databaseClass").build())
                .addException(NoSuchFieldException.class)
                .addException(IllegalAccessException.class)
                .returns(TypeName.VOID);

        switch (db.type()) {
            case MYSQL:
                break;
            case MONGODB:
                builder.addCode("""
                        com.mongodb.client.MongoDatabase database = client.getDatabase("%s");
                        com.mongodb.client.MongoCollection<org.bson.Document> collection = database.getCollection("%s");
                        
                        org.bson.Document old = collection.find(com.mongodb.client.model.Filters.eq("%s", databaseClass.getClass().getField("%s").get(databaseClass))).first();
                        if (old == null) {
                            collection.insertOne(org.bson.Document.parse(gson.toJson(databaseClass)));
                            return;
                        }
                        
                        collection.replaceOne(old, org.bson.Document.parse(gson.toJson(databaseClass)));
                        """.formatted(db.type().getDatabase(), db.table(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName())
                );
                break;
            default:
                //builder.addCode("System.out.println(\"ERROR: Database type is null\");");
                break;
        }

        return builder.build();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}

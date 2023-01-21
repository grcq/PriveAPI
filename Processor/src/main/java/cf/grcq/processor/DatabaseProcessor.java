package cf.grcq.processor;

import cf.grcq.processor.annotations.Database;
import cf.grcq.processor.database.Filter;
import cf.grcq.processor.database.Type;
import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.MetaInfServices;

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
        MethodSpec getMethod = generateGetMethod(fields, target, name, db);
        MethodSpec deleteMethod = generateDeleteMethod(fields, target, name, db);

        TypeSpec.Builder builder = TypeSpec.classBuilder(type.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(initMethod)
                .addMethod(saveMethod)
                .addMethod(getMethod)
                .addMethod(deleteMethod)
                .addField(FieldSpec.builder(Gson.class, "gson", Modifier.PRIVATE, Modifier.STATIC).build());

        if (db.type() == Type.MONGODB) {
            builder.addField(FieldSpec.builder(MongoClient.class, "client", Modifier.PRIVATE, Modifier.STATIC).build());
        }

        TypeSpec t = builder.build();

        JavaFile file = JavaFile.builder(type.packageName(), t.toBuilder().build()).build();

        file.writeTo(filer);
    }

    private MethodSpec generateInitMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(Class.class, "databaseClass").addAnnotation(NotNull.class).build())
                .returns(TypeName.VOID);

        switch (db.type()) {
            case MYSQL:
                builder.addCode("""
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            
                            java.lang.StringBuilder stringBuilder = new java.lang.StringBuilder();
                            
                            int i = 0;
                            for (java.lang.reflect.Field field : databaseClass.getDeclaredFields()) {
                                i++;
                                
                                String column;
                                switch (field.getName()) {
                                    case "int":
                                    case "Integer":
                                        column = "INT";
                                        break;
                                    case "float":
                                    case "Float":
                                    case "double":
                                    case "Double":
                                        column = "FLOAT";
                                        break;
                                    case "boolean":
                                    case "Boolean":
                                        column = "bool";
                                        break;
                                    case "long":
                                    case "Long":
                                        column = "BIGINT";
                                        break;
                                        
                                    default:
                                        column = "VARCHAR(128)";
                                        break;
                                }
                                
                                stringBuilder.append(field.getName()).append(" ").append(column);
                                
                                if (i < databaseClass.getDeclaredFields().length) stringBuilder.append(", ");
                            }
                            
                            java.sql.Connection connection = java.sql.DriverManager.getConnection(((cf.grcq.processor.annotations.Database) databaseClass.getAnnotation(cf.grcq.processor.annotations.Database.class)).type().getUri());
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS %s ();");
                            preparedStatement.executeUpdate();
                            
                            connection.close();
                        } catch (java.lang.ClassNotFoundException | java.sql.SQLException e) {
                            e.printStackTrace();
                        }
                        
                        gson = new com.google.gson.GsonBuilder().setLongSerializationPolicy(com.google.gson.LongSerializationPolicy.STRING).create();
                        """.formatted(db.table())
                );
                break;
            case MONGODB:
                builder.addCode("""
                        client = com.mongodb.client.MongoClients.create(((cf.grcq.processor.annotations.Database) databaseClass.getAnnotation(cf.grcq.processor.annotations.Database.class)).type().getUri());
                        gson = new com.google.gson.GsonBuilder().setLongSerializationPolicy(com.google.gson.LongSerializationPolicy.STRING).create();
                        """
                );
            default:
                builder.addCode("""
                        System.out.println("ERROR: Database type is null");
                        """);
                break;

        }

        return builder.build();
    }

    private MethodSpec generateSaveMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(typeName, "databaseClass").addAnnotation(NotNull.class).build())
                .returns(TypeName.VOID);

        switch (db.type()) {
            case MYSQL:
                builder.addCode("""
                        try {
                            java.sql.Connection connection = java.sql.DriverManager.getConnection(((cf.grcq.processor.annotations.Database) databaseClass.getClass().getAnnotation(cf.grcq.processor.annotations.Database.class)).type().getUri());
                            
                            java.lang.StringBuilder stringBuilder = new java.lang.StringBuilder();
                            java.lang.reflect.Field field_ = databaseClass.getClass().getDeclaredFields()[0];
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM %s WHERE " + field_.getName() + "=" + field_.get(databaseClass) + ";");
                            
                            if (!preparedStatement.executeQuery().next()) {
                                int i = 0;
                                for (java.lang.reflect.Field field : databaseClass.getClass().getDeclaredFields()) {
                                    i++;
                                    
                                    if (field.getName().equalsIgnoreCase("String")) stringBuilder.append("'").append(field.get(databaseClass)).append("'");
                                    else stringBuilder.append(field.get(databaseClass));
                                    
                                    if (i < databaseClass.getClass().getDeclaredFields().length) stringBuilder.append(", ");
                                }
                                
                                preparedStatement = connection.prepareStatement("INSERT INTO %s VALUES (" + stringBuilder.toString() + ");");
                                preparedStatement.executeUpdate();
                                
                                connection.close();
                                
                                return;
                            }
                            
                            int i = 0;
                            for (java.lang.reflect.Field field : databaseClass.getClass().getDeclaredFields()) {
                                i++;
                                
                                stringBuilder.append(field.getName()).append("=");
                                if (field.getName().equalsIgnoreCase("String")) stringBuilder.append("'").append(field.get(databaseClass)).append("'");
                                else stringBuilder.append(field.get(databaseClass));
                                
                                if (i < databaseClass.getClass().getDeclaredFields().length) stringBuilder.append(", ");
                            }
                            
                            preparedStatement = connection.prepareStatement("UPDATE %s SET " + stringBuilder + " WHERE %s='" + databaseClass.getClass().getDeclaredField("%s").get(databaseClass) + "';"); 
                            preparedStatement.executeUpdate();
                            
                            connection.close();
                            
                        } catch (java.sql.SQLException | NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        """.formatted(db.table(), db.table(), db.table(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName())
                );
                break;
            case MONGODB:
                builder.addCode("""
                        com.mongodb.client.MongoDatabase database = client.getDatabase("%s");
                        com.mongodb.client.MongoCollection<org.bson.Document> collection = database.getCollection("%s");
                        
                        try {
                            org.bson.Document old = collection.find(com.mongodb.client.model.Filters.eq("%s", databaseClass.getClass().getField("%s").get(databaseClass))).first();
                            if (old == null) {
                                collection.insertOne(org.bson.Document.parse(gson.toJson(databaseClass)));
                                return;
                            }
                            
                            collection.replaceOne(old, org.bson.Document.parse(gson.toJson(databaseClass)));
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        
                        """.formatted(db.type().getDatabase(), db.table(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName())
                );
                break;
            default:
                builder.addCode("""
                        System.out.println("ERROR: Database type is null");
                        """);
                break;
        }

        return builder.build();
    }

    private MethodSpec generateGetMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(Filter.class, "filter").addAnnotation(NotNull.class).build())
                .addAnnotation(Nullable.class)
                .returns(typeName);

        switch (db.type()) {
            case MYSQL:
                builder.addParameter(ParameterSpec.builder(typeName, "databaseClass").addAnnotation(NotNull.class).build())
                        .addCode("""
                        try {
                            java.sql.Connection connection = java.sql.DriverManager.getConnection(((cf.grcq.processor.annotations.Database) databaseClass.getClass().getAnnotation(cf.grcq.processor.annotations.Database.class)).type().getUri());
                            
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM %s WHERE " + filter.o1 + "=" + (filter.o2.getClass().getSimpleName().equalsIgnoreCase("String") ? "'" + filter.o2 + "'" : filter.o2 + ";")); 
                            java.sql.ResultSet resultSet = preparedStatement.executeQuery();
                            
                            com.google.gson.JsonObject object = new com.google.gson.JsonObject();
                            while (resultSet.next()) {
                                for (java.lang.reflect.Field field : databaseClass.getClass().getDeclaredFields()) {
                                    object.add(field.getName(), cf.grcq.processor.util.JsonUtil.fix(field.get(databaseClass)));
                                }
                            }
                            
                            connection.close();
                            
                            return gson.fromJson(object.toString(), %s.class);
                        } catch (java.sql.SQLException | IllegalAccessException e) {
                            e.printStackTrace();
                            return null;
                        }
                        """.formatted(db.table(), typeName)
                );
                break;
            case MONGODB:
                builder.addCode("""
                        com.mongodb.client.MongoDatabase database = client.getDatabase("%s");
                        com.mongodb.client.MongoCollection<org.bson.Document> collection = database.getCollection("%s");
                        
                        org.bson.Document found = collection.find(com.mongodb.client.model.Filters.eq(filter.o1, filter.o2)).first();
                                       
                        return (found == null ? null : gson.fromJson(found.toJson(), %s.class));
                        """.formatted(db.type().getDatabase(), db.table(), typeName)
                );
                break;
            default:
                builder.addCode("""
                        System.out.println("ERROR: Database type is null");
                        """);
                break;
        }

        return builder.build();
    }

    private MethodSpec generateDeleteMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(typeName, "databaseClass").addAnnotation(NotNull.class).build())
                .addAnnotation(Nullable.class)
                .returns(TypeName.VOID);
        switch (db.type()) {
            case MYSQL:
                builder.addCode("""
                        try {
                            java.sql.Connection connection = java.sql.DriverManager.getConnection(((cf.grcq.processor.annotations.Database) databaseClass.getClass().getAnnotation(cf.grcq.processor.annotations.Database.class)).type().getUri());
                            
                            cf.grcq.processor.database.Filter filter = cf.grcq.processor.database.Filter.equals("%s", databaseClass.getClass().getField("%s").get(databaseClass));
                            
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM %s WHERE " + filter.o1 + "=" + (filter.o2.getClass().getSimpleName().equalsIgnoreCase("String") ? "'" + filter.o2 + "'" : filter.o2 + ";")); 
                            preparedStatement.executeUpdate();
                        } catch (java.sql.SQLException | NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        """.formatted(fields.get(0).getSimpleName(), fields.get(0).getSimpleName(), db.table())
                );
                break;
            case MONGODB:
                builder.addCode("""
                        com.mongodb.client.MongoDatabase database = client.getDatabase("%s");
                        com.mongodb.client.MongoCollection<org.bson.Document> collection = database.getCollection("%s");
                                                
                        try {
                            org.bson.Document old = collection.find(com.mongodb.client.model.Filters.eq("%s", databaseClass.getClass().getField("%s").get(databaseClass))).first();
                            if (old == null) {
                                return;
                            }
                            
                            collection.deleteOne(org.bson.Document.parse(gson.toJson(databaseClass)));
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        """.formatted(db.type().getDatabase(), db.table(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName()));
            default:
                builder.addCode("""
                        System.out.println("ERROR: Database type is null");
                        """);
                break;
        }

        return builder.build();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}

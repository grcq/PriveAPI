package cf.grcq.processor;

import cf.grcq.processor.annotations.Database;
import cf.grcq.processor.database.Filter;
import cf.grcq.processor.database.Type;
import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.squareup.javapoet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
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

        try {
            MethodSpec saveMethod = generateSaveMethod(fields, target, name, db);
            MethodSpec initMethod = generateInitMethod(fields, target, name, db);
            MethodSpec getMethod = generateGetMethod(fields, target, name, db);
            MethodSpec getAllMethod = generateGetAllMethod(fields, target, name, db);
            MethodSpec deleteMethod = generateDeleteMethod(fields, target, name, db);

            TypeSpec.Builder builder = TypeSpec.classBuilder(type.simpleName())
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(initMethod)
                    .addMethod(saveMethod)
                    .addMethod(getMethod)
                    .addMethod(getAllMethod)
                    .addMethod(deleteMethod)
                    .addField(FieldSpec.builder(Gson.class, "gson", Modifier.PRIVATE, Modifier.STATIC).build());

            if (db.type() == Type.MONGODB) {
                builder.addField(FieldSpec.builder(MongoClient.class, "client", Modifier.PRIVATE, Modifier.STATIC).build());
            }

            TypeSpec t = builder.build();

            JavaFile file = JavaFile.builder(type.packageName(), t.toBuilder().build()).build();

            file.writeTo(filer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MethodSpec generateInitMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(Class.class, "databaseClass").addAnnotation(NotNull.class).build())
                .returns(TypeName.VOID);

        switch (db.type()) {
            case MYSQL:
                String uri = "jdbc:mysql://" +
                        db.address() +
                        "/" +
                        db.database();
                StringBuilder stringBuilder = new StringBuilder();

                int i = 0;
                for (Element field : fields) {
                    i++;

                    String column;
                    switch (field.asType().toString()) {
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

                    stringBuilder.append(field.getSimpleName().toString()).append(" ").append(column);

                    if (i < fields.size()) stringBuilder.append(", ");
                }

                builder.addCode(String.format("""
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            
                            java.sql.Connection connection = java.sql.DriverManager.getConnection("%s", "%s", "%s");
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS %s (%s);");
                            
                            preparedStatement.executeUpdate();
                            
                            connection.close();
                        } catch (java.lang.ClassNotFoundException | java.sql.SQLException e) {
                            e.printStackTrace();
                        }
                        
                        gson = new com.google.gson.GsonBuilder().setLongSerializationPolicy(com.google.gson.LongSerializationPolicy.STRING).create();
                        """, uri, db.username(), db.password(), db.table(), stringBuilder)
                );
                break;
            case MONGODB:
                uri = "mongodb" + (db.srv() ? "+srv" : "") + "://"  + db.username() + ":" + db.password() + "@" + db.address() + "/" + db.database() + "?w=majority&retryWrites=true";
                builder.addCode(String.format("""
                        client = com.mongodb.client.MongoClients.create("%s");
                        gson = new com.google.gson.GsonBuilder().setLongSerializationPolicy(com.google.gson.LongSerializationPolicy.STRING).create();
                        """, uri)
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

        String uri = "jdbc:mysql://" +
                db.address() +
                "/" +
                db.database();

        switch (db.type()) {
            case MYSQL:
                builder.addCode(String.format("""
                        try {
                            java.sql.Connection connection = java.sql.DriverManager.getConnection("%s", "%s", "%s");
                            
                            java.lang.StringBuilder stringBuilder = new java.lang.StringBuilder();
                            java.lang.reflect.Field field_ = databaseClass.getClass().getDeclaredFields()[0];
                            field_.setAccessible(true);
                            
                            Object when;
                            if (field_.getType().getSimpleName().equalsIgnoreCase("String") || field_.getType().getSimpleName().equalsIgnoreCase("UUID")) {
                                Object o = field_.get(databaseClass);
                                
                                if (o == null) when = "null";
                                else when = String.valueOf("'" + field_.get(databaseClass).toString() + "'");
                            } else when = field_.get(databaseClass);
                            
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM %s WHERE " + field_.getName() + "=" + when + ";");
                            field_.setAccessible(false);
                            
                            if (!preparedStatement.executeQuery().next()) {
                                int i = 0;
                                for (java.lang.reflect.Field field : databaseClass.getClass().getDeclaredFields()) {
                                    i++;
                                    
                                    field.setAccessible(true);
                                    
                                    Object object;
                                    System.out.println(field.getType().getSimpleName());
                                    if (field.getType().getSimpleName().equalsIgnoreCase("String") || field.getType().getSimpleName().equalsIgnoreCase("UUID")) {
                                        Object o = field.get(databaseClass);
                                        System.out.println(o);
                                        
                                        if (o == null) object = "null";
                                        else object = String.valueOf("'" + field.get(databaseClass).toString() + "'");
                                    } else object = field.get(databaseClass);
                                    
                                    System.out.println(object);
                                    
                                    stringBuilder.append(String.valueOf(object));
                                    
                                    field.setAccessible(false);
                                    
                                    if (i < databaseClass.getClass().getDeclaredFields().length) stringBuilder.append(", ");
                                }
                                
                                preparedStatement = connection.prepareStatement("INSERT INTO %s VALUES (" + stringBuilder.toString() + ");");
                                System.out.println(stringBuilder.toString() + " - STRIGNB BUIOLDER");
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
                        """, uri, db.username(), db.password(), db.table(), db.table(), db.table(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName())
                );
                break;
            case MONGODB:
                builder.addCode(String.format("""
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
                        
                        """, db.database(), db.table(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName())
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

        String uri = "jdbc:mysql://" +
                db.address() +
                "/" +
                db.database();

        switch (db.type()) {
            case MYSQL:
                builder.addParameter(ParameterSpec.builder(Class.class, "databaseClass").addAnnotation(NotNull.class).build())
                        .addCode(String.format("""
                        try {
                            java.sql.Connection connection = java.sql.DriverManager.getConnection("%s", "%s", "%s");
                            
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM %s WHERE " + filter.o1 + "=" + (filter.o2.getClass().getSimpleName().equalsIgnoreCase("String") || filter.o2.getClass().getSimpleName().equalsIgnoreCase("UUID") ? "'" + filter.o2 + "'" : filter.o2.toString() + ";")); 
                            java.sql.ResultSet resultSet = preparedStatement.executeQuery();
                            
                            com.google.gson.JsonObject object = new com.google.gson.JsonObject();
                            while (resultSet.next()) {
                                for (java.lang.reflect.Field field : databaseClass.getDeclaredFields()) {
                                    field.setAccessible(true);
                                    object.add(field.getName(), cf.grcq.processor.util.JsonUtil.fix(field.get(databaseClass)));
                                    field.setAccessible(false);
                                }
                            }
                            
                            connection.close();
                            
                            return gson.fromJson(object.toString(), %s.class);
                        } catch (java.sql.SQLException | IllegalAccessException e) {
                            e.printStackTrace();
                            return null;
                        }
                        """, uri, db.username(), db.password(), db.table(), typeName)
                );
                break;
            case MONGODB:
                builder.addCode(String.format("""
                        com.mongodb.client.MongoDatabase database = client.getDatabase("%s");
                        com.mongodb.client.MongoCollection<org.bson.Document> collection = database.getCollection("%s");
                        
                        org.bson.Document found = collection.find(com.mongodb.client.model.Filters.eq(filter.o1, filter.o2)).first();
                                       
                        return (found == null ? null : gson.fromJson(found.toJson(), %s.class));
                        """, db.database(), db.table(), typeName)
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

        String uri = "jdbc:mysql://" +
                db.address() +
                "/" +
                db.database();

        switch (db.type()) {
            case MYSQL:
                builder.addCode(String.format("""
                        try {
                            java.sql.Connection connection = java.sql.DriverManager.getConnection("%s", "%s", "%s");
                            
                            cf.grcq.processor.database.Filter filter = cf.grcq.processor.database.Filter.equal("%s", databaseClass.getClass().getField("%s").get(databaseClass));
                            
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM %s WHERE " + filter.o1 + "=" + (filter.o2.getClass().getSimpleName().equalsIgnoreCase("String") ? "'" + filter.o2 + "'" : filter.o2 + ";")); 
                            preparedStatement.executeUpdate();
                            
                            connection.close();
                        } catch (java.sql.SQLException | NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        """, uri, db.username(), db.password(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName(), db.table())
                );
                break;
            case MONGODB:
                builder.addCode(String.format("""
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
                        """, db.database(), db.table(), fields.get(0).getSimpleName(), fields.get(0).getSimpleName()));
            default:
                builder.addCode("""
                        System.out.println("ERROR: Database type is null");
                        """);
                break;
        }

        return builder.build();
    }

    private MethodSpec generateGetAllMethod(List<Element> fields, TypeName typeName, String name, Database db) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(Class.class, "databaseClass").addAnnotation(NotNull.class).build())
                .addAnnotation(Nullable.class)
                .returns(List.class);

        String uri = "jdbc:mysql://" +
                db.username() +
                ":" +
                db.password() +
                "@" +
                db.address() +
                "/" +
                db.database();

        switch (db.type()) {
            case MONGODB:
                builder.addCode(String.format("""
                        com.mongodb.client.MongoDatabase database = client.getDatabase("%s");
                        com.mongodb.client.MongoCollection<org.bson.Document> collection = database.getCollection("%s");
                        
                        java.util.List<%s> list = new java.util.ArrayList<>();
                        for (org.bson.Document document : collection.find()) {
                            %s value = gson.fromJson(document.toJson(), %s.class);
                            list.add(value)
                        }
                        
                        return list;
                        """, db.database(), db.table(), typeName, typeName, typeName)
                );
                break;
            case MYSQL:
                builder.addCode(String.format("""
                        try {
                            java.sql.Connection connection = java.sql.DriverManager.getConnection("%s", "%s", "%s");
                            
                            java.sql.PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM %s;"); 
                            java.sql.ResultSet resultSet = preparedStatement.executeQuery();
                        
                            java.util.List<%s> list = new java.util.ArrayList<>();
                            while (resultSet.next()) {
                                com.google.gson.JsonObject object = new com.google.gson.JsonObject();
                                for (java.lang.reflect.Field field : databaseClass.getDeclaredFields()) {
                                    field.setAccessible(true);
                                    object.add(field.getName(), cf.grcq.processor.util.JsonUtil.fix(field.get(databaseClass)));
                                    field.setAccessible(false);
                                }
                                
                                %s value = gson.fromJson(object.toString(), %s.class);
                                list.add(value);
                            }
                            
                            connection.close();
                            
                            return list;
                        } catch (java.sql.SQLException | IllegalAccessException e) {
                            e.printStackTrace();
                            return new java.util.ArrayList<>();
                        }
                        """, uri, db.username(), db.password(), db.table(), typeName, typeName, typeName)
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

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}

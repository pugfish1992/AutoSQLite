package com.pugfish1992.autosqlite;

import android.support.annotation.Nullable;

import com.pugfish1992.autosqlite.annotation.CurrentVersion;
import com.pugfish1992.autosqlite.annotation.Database;
import com.pugfish1992.autosqlite.annotation.Field;
import com.pugfish1992.autosqlite.annotation.Entity;
import com.pugfish1992.autosqlite.core.AffinityType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

@SupportedAnnotationTypes("com.pugfish1992.autosqlite.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AutoSQLiteProcessor extends AbstractProcessor {

    private static final String PACKAGE_TO_GENERATE = "com.pugfish1992.autosqlite.build";

    private static final String DIFF_CLASS_SUFFIX = "Diff";
    private static final String SOURCE_CLASS_SUFFIX = "Source";
    private static final String IMPL_CLASS_SUFFIX = "Impl";
    private static final String NULL_CLASS_PREFIX = "Null";

    private static final String OPEN_HELPER_CLASS_SUFFIX = "OpenHelper";

    private static final String PRIMARY_KEY_FIELD_NAME = "id";
    private static final Set<String> RESERVED_WORDS;
    static {
        RESERVED_WORDS = new HashSet<>();
        RESERVED_WORDS.add(PRIMARY_KEY_FIELD_NAME);
        RESERVED_WORDS.add("isNull");
        RESERVED_WORDS.add("isEnable");
    }

    private Filer mFiler;
    private MessagerHelper mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = new MessagerHelper(processingEnvironment.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        //TODO;
        return newProcess(roundEnvironment);

//        List<EntityInfo> entityInfoList = new ArrayList<>();
//
//        for (Element element : roundEnvironment.getElementsAnnotatedWith(Entity.class)) {
//            if (element.getKind() != ElementKind.CLASS) {
//                mMessager.error(element, "%s can be applied only to class", Entity.class);
//                continue;
//            }
//
//            EntityInfo entityInfo = new EntityInfo();
//            entityInfo.otherFields = new ArrayList<>();
//
//            entityInfo.primaryKeyField = new FieldInfo();
//            entityInfo.primaryKeyField.columnName = com.pugfish1992.autosqlite.core.Entity.PRIMARY_KEY_COLUMN;
//            entityInfo.primaryKeyField.columnType = AffinityType.INTEGER;
//            entityInfo.primaryKeyField.fieldName = PRIMARY_KEY_FIELD_NAME;
//            entityInfo.primaryKeyField.fieldType = TypeName.LONG;
//            entityInfo.primaryKeyField.defaultValue = com.pugfish1992.autosqlite.core.Entity.INVALID_ID;
//
//            entityInfo.entityName = element.getAnnotation(Entity.class).value();
//            if (entityInfo.entityName.length() == 0) {
//                mMessager.error(element, "invalid entity name");
//                continue;
//            }
//
//            String interfaceName = toPascalCase(entityInfo.entityName);
//            String nullClassName = NULL_CLASS_PREFIX + interfaceName;
//            String implClassName = interfaceName + IMPL_CLASS_SUFFIX;
//            String diffClassName = interfaceName + DIFF_CLASS_SUFFIX;
//            String sourceClassName = interfaceName + SOURCE_CLASS_SUFFIX;
//            entityInfo.entityInterface = ClassName.get(PACKAGE_TO_GENERATE, interfaceName);
//            entityInfo.entityImplClass = ClassName.get(PACKAGE_TO_GENERATE, implClassName);
//            entityInfo.nullEntityClass = ClassName.get(PACKAGE_TO_GENERATE, nullClassName);
//            entityInfo.diffClass = ClassName.get(PACKAGE_TO_GENERATE, diffClassName);
//            entityInfo.sourceClass = ClassName.get(PACKAGE_TO_GENERATE, sourceClassName);
//
//            for (Element nestedElement : element.getEnclosedElements()) {
//                if (nestedElement.getKind() != ElementKind.FIELD) continue;
//
//                FieldInfo field = new FieldInfo();
//                field.fieldName = nestedElement.getSimpleName().toString();
//                if (RESERVED_WORDS.contains(field.fieldName)) {
//                    mMessager.error(nestedElement, String.format("cannot use '%s' as a variable name", field.fieldName));
//                }
//
//                field.fieldType = TypeName.get(nestedElement.asType());
//                if (!SupportedTypeUtils.isSupportedJavaType(field.fieldType)) {
//                    mMessager.error(nestedElement, "unsupported type '%s', this field was ignored", field.fieldType.toString());
//                    continue;
//                }
//
//                VariableElement varElement = (VariableElement) nestedElement;
//                field.defaultValue = varElement.getConstantValue();
//
//                Field fieldAnno = nestedElement.getAnnotation(Field.class);
//                if (fieldAnno == null) continue;
//                field.columnName = fieldAnno.value();
//                field.columnType = SupportedTypeUtils.affinityTypeFromSupportedJavaType(field.fieldType);
//
//                if (field.columnName.length() == 0) {
//                    mMessager.error(nestedElement, "invalid column name");
//                    continue;
//                }
//
//                entityInfo.otherFields.add(field);
//            }
//
//            entityInfoList.add(entityInfo);
//        }
//
//        ClassName openHelperClass = ClassName.get(PACKAGE_TO_GENERATE, OPEN_HELPER_CLASS_SUFFIX);
//
//        try {
//            for (EntityInfo entityInfo : entityInfoList) {
//                InterfaceWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
//                ImplClassWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
//                NullClassWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
//                DiffClassWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
//                SourceClassWriter.write(entityInfo, openHelperClass, PACKAGE_TO_GENERATE, mFiler);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            mMessager.error("error creating java file");
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//            mMessager.error("internal error in code generation");
//        }

//        return false;
    }

    /**
     * Convert a snake-case or a camel-case to a pascal-case(upper-camel-case).
     * @param name Expect a snake-case or a camel-case.
     */
    private String toPascalCase(String name) {
        StringBuilder pascal = new StringBuilder();
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                pascal.append(c);
            } else {
                if (Character.isDigit(c)) {
                    pascal.append(c);
                }
                if (i + 1 < name.length() && Character.isLetter(name.charAt(i + 1))) {
                    pascal.append(Character.toUpperCase(name.charAt(i + 1)));
                    ++i;
                }
            }
        }

        if (0 < pascal.length()) {
            char c = Character.toUpperCase(pascal.charAt(0));
            pascal.replace(0, 1, Character.toString(c));
        }

        return pascal.toString();
    }

    private boolean newProcess(RoundEnvironment roundEnvironment) {

        // key is a db name
        Set<String> databaseNames = new HashSet<>();
        Map<String, DatabaseRecipe> databaseRecipesOfCurrentVersion = new HashMap<>();
        Map<String, Set<DatabaseRecipe>> versionedDatabaseRecipes = new HashMap<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Database.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                mMessager.error(element, "%s can be applied only to class", Database.class);
                return false;
            }

            Database databaseAnno = element.getAnnotation(Database.class);
            final String dbName = databaseAnno.name();
            final int dbVersion = databaseAnno.version();
            if (dbName.length() == 0) {
                mMessager.error(element, "Please specify database name");
                return false;
            }
            databaseNames.add(dbName);

            DatabaseRecipe databaseRecipe = new DatabaseRecipe(dbName, dbVersion);
            if (!versionedDatabaseRecipes.containsKey(dbName)) {
                versionedDatabaseRecipes.put(dbName, new HashSet<DatabaseRecipe>());
            }
            versionedDatabaseRecipes.get(dbName).add(databaseRecipe);

            CurrentVersion currentVersionAnno = element.getAnnotation(CurrentVersion.class);
            if (currentVersionAnno != null) {
                if (databaseRecipesOfCurrentVersion.containsKey(dbName)) {
                    mMessager.error(element, "ambiguous current db version of '%s'", dbName);
                    return false;
                }
                databaseRecipesOfCurrentVersion.put(dbName, databaseRecipe);
            }

            for (Element nestedElement : element.getEnclosedElements()) {
                EntityRecipe entityRecipe = extractEntityRecipeIfPossibleFrom(nestedElement);
                if (entityRecipe != null) {
                    databaseRecipe.addEntityRecipe(entityRecipe);
                }
            }
        }

        for (String dbName : databaseNames) {
            DatabaseRecipe databaseRecipe = databaseRecipesOfCurrentVersion.get(dbName);
            if (databaseRecipe == null) {
                mMessager.error("current db version of '%s' is not specified", dbName);
                return false;
            }

            try {
                writeClassesForDatabase(databaseRecipe, versionedDatabaseRecipes.get(dbName));
            } catch (IOException e) {
                e.printStackTrace();
                mMessager.error("error creating java file");
            } catch (RuntimeException e) {
                e.printStackTrace();
                mMessager.error("internal error in code generation");
            }
        }

        return false;
    }

    private void writeClassesForDatabase(DatabaseRecipe currentVersionDbRecipe,
                                         Set<DatabaseRecipe> versionedDbRecipes) throws IOException, RuntimeException {

        final String dbName = currentVersionDbRecipe.name;
        String openHelperClassName = toPascalCase(dbName) + OPEN_HELPER_CLASS_SUFFIX;
        ClassName openHelperClass = ClassName.get(PACKAGE_TO_GENERATE, openHelperClassName);

        for (EntityRecipe entityRecipe : currentVersionDbRecipe.entityRecipes) {
            String interfaceName = toPascalCase(entityRecipe.name);
            String nullClassName = NULL_CLASS_PREFIX + interfaceName;
            String implClassName = interfaceName + IMPL_CLASS_SUFFIX;
            String diffClassName = interfaceName + DIFF_CLASS_SUFFIX;
            String sourceClassName = interfaceName + SOURCE_CLASS_SUFFIX;
            ClassName entityInterface = ClassName.get(PACKAGE_TO_GENERATE, interfaceName);
            ClassName entityImplClass = ClassName.get(PACKAGE_TO_GENERATE, implClassName);
            ClassName nullEntityClass = ClassName.get(PACKAGE_TO_GENERATE, nullClassName);
            ClassName diffClass = ClassName.get(PACKAGE_TO_GENERATE, diffClassName);
            ClassName sourceClass = ClassName.get(PACKAGE_TO_GENERATE, sourceClassName);

            try {
                InterfaceWriter.write(entityRecipe, entityInterface, PACKAGE_TO_GENERATE, mFiler);
                ImplClassWriter.write(entityRecipe, entityImplClass, entityInterface, PACKAGE_TO_GENERATE, mFiler);
                NullClassWriter.write(entityRecipe, entityInterface, nullEntityClass, PACKAGE_TO_GENERATE, mFiler);
                DiffClassWriter.write(entityRecipe, entityImplClass, diffClass, entityInterface, PACKAGE_TO_GENERATE, mFiler);
                SourceClassWriter.write(entityRecipe, sourceClass, entityInterface,
                        entityImplClass, nullEntityClass, diffClass, openHelperClass, PACKAGE_TO_GENERATE, mFiler);
            } catch (IOException e) {
                e.printStackTrace();
                mMessager.error("error creating java file");
            } catch (RuntimeException e) {
                e.printStackTrace();
                mMessager.error("internal error in code generation");
            }
        }
    }

    @Nullable
    private EntityRecipe extractEntityRecipeIfPossibleFrom(Element superElement) {
        Entity entityAnno = superElement.getAnnotation(Entity.class);
        if (entityAnno == null) return null;
        if (superElement.getKind() != ElementKind.CLASS) {
            mMessager.error(superElement, "%s can be applied only to class", Entity.class);
            return null;
        }

        FieldRecipe pkFieldRecipe = new FieldRecipe(
                PRIMARY_KEY_FIELD_NAME,
                TypeName.LONG,
                com.pugfish1992.autosqlite.core.Entity.PRIMARY_KEY_COLUMN,
                AffinityType.INTEGER,
                com.pugfish1992.autosqlite.core.Entity.INVALID_ID);

        String entityName = entityAnno.value();
        if (entityName.length() == 0) {
            mMessager.error(superElement, "invalid entity name");
            return null;
        }

        EntityRecipe entityRecipe = new EntityRecipe(entityName, pkFieldRecipe);

        for (Element element : superElement.getEnclosedElements()) {
            Field fieldAnno = element.getAnnotation(Field.class);
            if (fieldAnno == null) continue;
            if (element.getKind() != ElementKind.FIELD) {
                mMessager.error(superElement, "%s can be applied only to class", Field.class);
                return null;
            }

            final String fieldName = element.getSimpleName().toString();
            if (RESERVED_WORDS.contains(fieldName)) {
                mMessager.error(element, String.format("cannot use '%s' as a variable name", fieldName));
            }

            final TypeName fieldType = TypeName.get(element.asType());
            if (!SupportedTypeUtils.isSupportedJavaType(fieldType)) {
                mMessager.error(element, "unsupported type > %s", fieldType.toString());
                return null;
            }

            final String columnName = fieldAnno.value();
            if (columnName.length() == 0) {
                mMessager.error(element, "invalid column name");
                return null;
            }

            final AffinityType columnType = SupportedTypeUtils.affinityTypeFromSupportedJavaType(fieldType);
            final Object defaultValue = ((VariableElement) element).getConstantValue();

            FieldRecipe fieldRecipe = new FieldRecipe(fieldName, fieldType, columnName, columnType, defaultValue);
            entityRecipe.addFieldRecipe(fieldRecipe);
        }

        return entityRecipe;
    }
}

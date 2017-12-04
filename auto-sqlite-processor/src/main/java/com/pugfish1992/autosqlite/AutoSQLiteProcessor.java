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

    private static final int INVALID_DB_VERSION = -1992;

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

        // key is a db name
        Map<String, VersionedDatabaseRecipes> dbNamesWithVersionedDbRecipes = new HashMap<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Database.class)) {
            DatabaseRecipe databaseRecipe = extractDatabaseRecipeIfPossibleFrom(element);
            if (databaseRecipe == null) continue;

            if (dbNamesWithVersionedDbRecipes.containsKey(databaseRecipe.getName())) {
                VersionedDatabaseRecipes versionedDatabaseRecipes = dbNamesWithVersionedDbRecipes.get(databaseRecipe.getName());
                if (versionedDatabaseRecipes.findDatabaseRecipeByVersion(databaseRecipe.getVersion()) != null) {
                    mMessager.error(element, "ambiguous db version of '%s'", databaseRecipe.getName());
                    return false;
                }
                versionedDatabaseRecipes.addDatabaseRecipe(databaseRecipe);

            } else {
                VersionedDatabaseRecipes versionedDatabaseRecipes = new VersionedDatabaseRecipes(databaseRecipe.getName());
                versionedDatabaseRecipes.setCurrentVersion(INVALID_DB_VERSION);
                versionedDatabaseRecipes.addDatabaseRecipe(databaseRecipe);
                dbNamesWithVersionedDbRecipes.put(databaseRecipe.getName(), versionedDatabaseRecipes);
            }

            CurrentVersion currentVersionAnno = element.getAnnotation(CurrentVersion.class);
            if (currentVersionAnno != null) {
                VersionedDatabaseRecipes versionedDatabaseRecipes = dbNamesWithVersionedDbRecipes.get(databaseRecipe.getName());
                if (versionedDatabaseRecipes.getCurrentVersion() != INVALID_DB_VERSION) {
                    mMessager.error(element, "ambiguous current db version of '%s'", databaseRecipe.getName());
                    return false;
                }
                versionedDatabaseRecipes.setCurrentVersion(databaseRecipe.getVersion());
            }
        }

        for (VersionedDatabaseRecipes versionedDatabaseRecipes : dbNamesWithVersionedDbRecipes.values()) {
            DatabaseRecipe currentVersionDbRecipe = versionedDatabaseRecipes.findCurrentVersionDatabaseRecipe();
            if (currentVersionDbRecipe == null) {
                mMessager.error("current db version of '%s' is not specified", versionedDatabaseRecipes.getDatabaseName());
                return false;
            }

            String openHelperClassName = toPascalCase(versionedDatabaseRecipes.getDatabaseName()) + OPEN_HELPER_CLASS_SUFFIX;
            ClassName openHelperClass = ClassName.get(PACKAGE_TO_GENERATE, openHelperClassName);

            try {
                for (EntityRecipe entityRecipe : currentVersionDbRecipe.getEntityRecipes()) {
                    String interfaceName = toPascalCase(entityRecipe.getName());
                    String nullClassName = NULL_CLASS_PREFIX + interfaceName;
                    String implClassName = interfaceName + IMPL_CLASS_SUFFIX;
                    String diffClassName = interfaceName + DIFF_CLASS_SUFFIX;
                    String sourceClassName = interfaceName + SOURCE_CLASS_SUFFIX;
                    ClassName entityInterface = ClassName.get(PACKAGE_TO_GENERATE, interfaceName);
                    ClassName entityImplClass = ClassName.get(PACKAGE_TO_GENERATE, implClassName);
                    ClassName nullEntityClass = ClassName.get(PACKAGE_TO_GENERATE, nullClassName);
                    ClassName diffClass = ClassName.get(PACKAGE_TO_GENERATE, diffClassName);
                    ClassName sourceClass = ClassName.get(PACKAGE_TO_GENERATE, sourceClassName);

                    InterfaceWriter.write(entityRecipe, entityInterface, PACKAGE_TO_GENERATE, mFiler);
                    ImplClassWriter.write(entityRecipe, entityImplClass, entityInterface, PACKAGE_TO_GENERATE, mFiler);
                    NullClassWriter.write(entityRecipe, entityInterface, nullEntityClass, PACKAGE_TO_GENERATE, mFiler);
                    DiffClassWriter.write(entityRecipe, entityImplClass, diffClass, entityInterface, PACKAGE_TO_GENERATE, mFiler);
                    SourceClassWriter.write(entityRecipe, sourceClass, entityInterface,
                            entityImplClass, nullEntityClass, diffClass, openHelperClass, PACKAGE_TO_GENERATE, mFiler);
                }

                OpenHelperWriter.write(currentVersionDbRecipe.getVersion(), versionedDatabaseRecipes, openHelperClass, PACKAGE_TO_GENERATE, mFiler);

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

    @Nullable
    private DatabaseRecipe extractDatabaseRecipeIfPossibleFrom(Element element) {

        if (element.getKind() != ElementKind.CLASS) {
            mMessager.error(element, "%s can be applied only to class", Database.class);
            return null;
        }

        Database databaseAnno = element.getAnnotation(Database.class);
        final String dbName = databaseAnno.name();
        final int dbVersion = databaseAnno.version();
        if (dbName.length() == 0) {
            mMessager.error(element, "Please specify database name");
            return null;
        }

        DatabaseRecipe databaseRecipe = new DatabaseRecipe(dbName, dbVersion);
        List<EntityRecipe> extractedEntityRecipes = new ArrayList<>();
        for (Element nestedElement : element.getEnclosedElements()) {
            EntityRecipe extracted = extractEntityRecipeIfPossibleFrom(nestedElement);
            if (extracted != null) {
                for (EntityRecipe entityRecipe : extractedEntityRecipes) {
                    if (entityRecipe.getName().equals(extracted.getName())) {
                        mMessager.error(nestedElement,
                                "'%s' is already defined in the database '%s'",
                                extracted.getName(), dbName);

                        return null;
                    }
                }
                extractedEntityRecipes.add(extracted);
            }
        }

        databaseRecipe.addEntityRecipes(extractedEntityRecipes);
        return databaseRecipe;
    }

    @Nullable
    private EntityRecipe extractEntityRecipeIfPossibleFrom(Element element) {
        Entity entityAnno = element.getAnnotation(Entity.class);
        if (entityAnno == null) return null;
        if (element.getKind() != ElementKind.CLASS) {
            mMessager.error(element, "%s can be applied only to class", Entity.class);
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
            mMessager.error(element, "invalid entity name");
            return null;
        }

        List<FieldRecipe> extractedFieldRecipes = new ArrayList<>();
        for (Element nestedElement : element.getEnclosedElements()) {
            FieldRecipe extracted = extractFieldRecipeIfPossibleFrom(nestedElement);
            if (extracted != null) {
                for (FieldRecipe fieldRecipe : extractedFieldRecipes) {
                    if (fieldRecipe.getColumnName().equals(extracted.getColumnName())) {
                        mMessager.error(nestedElement,
                                "'%s' is already defined in the Entity '%s'",
                                extracted.getColumnName(), entityName);
                        return null;
                    }
                }
                extractedFieldRecipes.add(extracted);
            }
        }

        EntityRecipe entityRecipe = new EntityRecipe(entityName, pkFieldRecipe);
        entityRecipe.addFieldRecipes(extractedFieldRecipes);
        return entityRecipe;
    }

    @Nullable
    private FieldRecipe extractFieldRecipeIfPossibleFrom(Element element) {
        Field fieldAnno = element.getAnnotation(Field.class);
        if (fieldAnno == null) return null;
        if (element.getKind() != ElementKind.FIELD) {
            mMessager.error(element, "%s can be applied only to class", Field.class);
            return null;
        }

        final String fieldName = element.getSimpleName().toString();
        if (RESERVED_WORDS.contains(fieldName)) {
            mMessager.error(element, String.format("cannot use the word '%s', this is one of the reserved words", fieldName));
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

        return new FieldRecipe(fieldName, fieldType, columnName, columnType, defaultValue);
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
}

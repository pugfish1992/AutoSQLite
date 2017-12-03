package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.annotation.Column;
import com.pugfish1992.autosqlite.annotation.Entity;
import com.pugfish1992.autosqlite.core.AffinityType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private static final String TABLE_CLASS_SUFFIX = "Table";
    private static final String IMPL_CLASS_SUFFIX = "Impl";
    private static final String NULL_CLASS_PREFIX = "Null";

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

        List<EntityInfo> entityInfoList = new ArrayList<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Entity.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                mMessager.error(element, "%s can be applied only to class", Entity.class);
                continue;
            }

            EntityInfo entityInfo = new EntityInfo();
            entityInfo.otherFields = new ArrayList<>();

            entityInfo.primaryKeyField = new FieldInfo();
            entityInfo.primaryKeyField.columnName = com.pugfish1992.autosqlite.core.Entity.PRIMARY_KEY_COLUMN;
            entityInfo.primaryKeyField.columnType = AffinityType.INTEGER;
            entityInfo.primaryKeyField.fieldName = PRIMARY_KEY_FIELD_NAME;
            entityInfo.primaryKeyField.fieldType = TypeName.LONG;
            entityInfo.primaryKeyField.defaultValue = com.pugfish1992.autosqlite.core.Entity.INVALID_ID;

            entityInfo.entityName = element.getAnnotation(Entity.class).value();
            if (entityInfo.entityName.length() == 0) {
                mMessager.error(element, "invalid entity name");
                continue;
            }

            String interfaceName = toPascalCase(entityInfo.entityName);
            String nullClassName = NULL_CLASS_PREFIX + interfaceName;
            String implClassName = interfaceName + IMPL_CLASS_SUFFIX;
            String diffClassName = interfaceName + DIFF_CLASS_SUFFIX;
            String tableClassName = interfaceName + TABLE_CLASS_SUFFIX;
            entityInfo.entityInterface = ClassName.get(PACKAGE_TO_GENERATE, interfaceName);
            entityInfo.entityImplClass = ClassName.get(PACKAGE_TO_GENERATE, implClassName);
            entityInfo.nullEntityClass = ClassName.get(PACKAGE_TO_GENERATE, nullClassName);
            entityInfo.diffClass = ClassName.get(PACKAGE_TO_GENERATE, diffClassName);
            entityInfo.tableClass = ClassName.get(PACKAGE_TO_GENERATE, tableClassName);

            for (Element nestedElement : element.getEnclosedElements()) {
                if (nestedElement.getKind() != ElementKind.FIELD) continue;

                FieldInfo field = new FieldInfo();
                field.fieldName = nestedElement.getSimpleName().toString();
                if (RESERVED_WORDS.contains(field.fieldName)) {
                    mMessager.error(nestedElement, String.format("cannot use '%s' as a variable name", field.fieldName));
                }

                field.fieldType = TypeName.get(nestedElement.asType());
                if (!SupportedTypeUtils.isSupportedJavaType(field.fieldType)) {
                    mMessager.error(nestedElement, "unsupported type '%s', this field was ignored", field.fieldType.toString());
                    continue;
                }

                VariableElement varElement = (VariableElement) nestedElement;
                field.defaultValue = varElement.getConstantValue();

                Column columnAnno = nestedElement.getAnnotation(Column.class);
                if (columnAnno == null) continue;
                field.columnName = columnAnno.value();
                field.columnType = SupportedTypeUtils.affinityTypeFromSupportedJavaType(field.fieldType);

                if (field.columnName.length() == 0) {
                    mMessager.error(nestedElement, "invalid column name");
                    continue;
                }

                entityInfo.otherFields.add(field);
            }

            entityInfoList.add(entityInfo);
        }

        try {
            for (EntityInfo entityInfo : entityInfoList) {
                InterfaceWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
                ImplClassWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
                NullClassWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
                DiffClassWriter.write(entityInfo, PACKAGE_TO_GENERATE, mFiler);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mMessager.error("error creating java file");
        } catch (RuntimeException e) {
            e.printStackTrace();
            mMessager.error("internal error in code generation");
        }

        return false;
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

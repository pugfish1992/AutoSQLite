package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.core.EntityImpl;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import static com.pugfish1992.autosqlite.SupportedTypeUtils.*;

/**
 * Created by daichi on 12/3/17.
 */

class ImplClassWriter {

    private static final String VARIABLE_NAME_PREFIX = "m_";

    static void write(EntityInfo entityInfo, String packageName, Filer filer)
            throws IOException, RuntimeException {

        List<FieldInfo> allFields = new ArrayList<>();
        allFields.add(entityInfo.primaryKeyField);
        allFields.addAll(entityInfo.otherFields);

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(entityInfo.entityImplClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(entityInfo.entityInterface);

        // Declare immutable fields
        for (FieldInfo fieldInfo : allFields) {
            classSpec.addField(FieldSpec
                    .builder(fieldInfo.fieldType, VARIABLE_NAME_PREFIX + fieldInfo.fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build());
        }

        // Define a protected all-args constructor
        MethodSpec.Builder method = MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED);
        for (FieldInfo fieldInfo : allFields) {
            method.addParameter(ParameterSpec.builder(fieldInfo.fieldType, fieldInfo.fieldName).build());
            method.addStatement("$L = $L", VARIABLE_NAME_PREFIX + fieldInfo.fieldName, fieldInfo.fieldName);
        }
        classSpec.addMethod(method.build());

        // Override id() method
        classSpec.addMethod(MethodSpec.methodBuilder(entityInfo.primaryKeyField.fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(entityInfo.primaryKeyField.fieldType)
                .addStatement("return $L", VARIABLE_NAME_PREFIX + entityInfo.primaryKeyField.fieldName)
                .build());

        // Override accessor methods
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            classSpec.addMethod(MethodSpec.methodBuilder(fieldInfo.fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(Override.class)
                    .returns(fieldInfo.fieldType)
                    .addStatement("return $L", VARIABLE_NAME_PREFIX + fieldInfo.fieldName)
                    .build());
        }

//        private boolean mIsEnable = true;

        classSpec.addField(FieldSpec
                .builder(TypeName.BOOLEAN, "mIsEnable")
                .addModifiers(Modifier.PRIVATE)
                .initializer("true")
                .build());

//        @Override
//        public final boolean isEnable() {
//            return mIsEnable;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("isEnable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addStatement("return mIsEnable")
                .build());

//        @Override
//        public final boolean isNull() {
//            return false;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("isNull")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addStatement("return false")
                .build());

//        protected final void disable() {
//            mIsEnable = false;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("disable")
                .addStatement("mIsEnable = false")
                .build());

        // TODO; Optimize equals()
        // Implement equals() method
        method = MethodSpec
                .methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(Types.OBJECT, "o")
                .addStatement("if (this == o) return true")
                .addStatement("if (o == null || getClass() != o.getClass()) return false")
                .addStatement("$T that = ($T) o", entityInfo.entityImplClass, entityInfo.entityImplClass);

        for (int i = 0; i < allFields.size(); ++i) {
            TypeName type = allFields.get(i).fieldType;
            String name = VARIABLE_NAME_PREFIX + allFields.get(i).fieldName;
            boolean isLast = (i + 1 == allFields.size());

            if (isIntType(type) || isLongType(type) || isBooleanType(type) || isByteType(type)) {
                if (isLast) method.addStatement("return $L == that.$L", name, name);
                else method.addStatement("if ($L != that.$L) return false", name, name);

            } else if (isFloatType(type)) {
                if (isLast) method.addStatement("return Float.compare(that.$L, $L) == 0", name, name);
                else method.addStatement("if (Float.compare(that.$L, $L) != 0) return false", name, name);

            } else if (isDoubleType(type)) {
                if (isLast) method.addStatement("return Double.compare(that.$L, $L) == 0", name, name);
                else method.addStatement("if (Double.compare(that.$L, $L) != 0) return false", name, name);

            } else if (isStringType(type)) {
                if (isLast) method.addStatement("return $L != null ? $L.equals(that.$L) : that.$L == null", name, name, name, name);
                else method.addStatement("if ($L != null ? !$L.equals(that.$L) : that.$L != null) return false", name, name, name, name);

            } else if (isByteArrayType(type)) {
                if (isLast) method.addStatement("return $T.equals($L, that.$L)", Types.ARRAYS, name, name);
                else method.addStatement("if (!$T.equals($L, that.$L)) return false", Types.ARRAYS, name, name);
            }
        }
        classSpec.addMethod(method.build());

        // TODO; Optimize hashCode()
        // Implement hashCode() method
        method = MethodSpec
                .methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addCode("return $L.hash(\n", Types.OBJECTS)
                .addCode("$L\n", VARIABLE_NAME_PREFIX + entityInfo.primaryKeyField.fieldName);

        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            method.addCode(",$L\n", VARIABLE_NAME_PREFIX + fieldInfo.fieldName);
        }

        method.addCode(");\n");
        classSpec.addMethod(method.build());


        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}
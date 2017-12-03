package com.pugfish1992.autosqlite;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by daichi on 12/3/17.
 */

class NullClassWriter {

    static void write(EntityInfo entityInfo, String packageName, Filer filer)
            throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(entityInfo.nullEntityClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(entityInfo.entityInterface);

        // Override accessor methods
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            Object returnValue = (fieldInfo.defaultValue != null)
                    ? fieldInfo.defaultValue
                    : SupportedTypeUtils.defaultValueOf(fieldInfo.fieldType);

            classSpec.addMethod(MethodSpec.methodBuilder(fieldInfo.fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(Override.class)
                    .returns(fieldInfo.fieldType)
                    .addStatement("return $L", Literal.of(returnValue))
                    .build());
        }

//        @Override
//        public final long id() {
//            return INVALID_ID;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("id")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.LONG)
                .addStatement("return INVALID_ID")
                .build());

//        @Override
//        public final boolean isEnable() {
//            return false;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("isEnable")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addStatement("return false")
                .build());

//        @Override
//        public final boolean isNull() {
//            return true;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("isNull")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN)
                .addStatement("return true")
                .build());

        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

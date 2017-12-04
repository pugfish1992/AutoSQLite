package com.pugfish1992.autosqlite;

import com.squareup.javapoet.ClassName;
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

    static void write(EntityRecipe entityRecipe, ClassName entityInterface, ClassName nullEntityClass, String packageName, Filer filer)
            throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(nullEntityClass)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(entityInterface);

        // Override accessor methods
        for (FieldRecipe fieldRecipe : entityRecipe.otherFieldRecipes) {
            Object returnValue = (fieldRecipe.defaultValue != null)
                    ? fieldRecipe.defaultValue
                    : SupportedTypeUtils.defaultValueOf(fieldRecipe.fieldType);

            classSpec.addMethod(MethodSpec.methodBuilder(fieldRecipe.fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(Override.class)
                    .returns(fieldRecipe.fieldType)
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

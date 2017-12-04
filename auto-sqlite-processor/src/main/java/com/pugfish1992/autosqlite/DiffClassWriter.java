package com.pugfish1992.autosqlite;

import android.support.annotation.NonNull;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by daichi on 12/3/17.
 */

class DiffClassWriter {
    private static final String VAR_NAME_PREFIX = "m_";
    private static final String FLAG_NAME_PREFIX = "mSet_";

    static void write(EntityRecipe entityRecipe, ClassName entityImplClass, ClassName diffClass,
                      ClassName entityInterface, String packageName, Filer filer) throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(diffClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // variables & setters
        for (FieldRecipe fieldRecipe : entityRecipe.otherFieldRecipes) {
            String varName = VAR_NAME_PREFIX + fieldRecipe.fieldName;
            String flagName = FLAG_NAME_PREFIX + fieldRecipe.fieldName;

            classSpec.addField(FieldSpec.builder(fieldRecipe.fieldType, varName)
                    .addModifiers(Modifier.PRIVATE).build());

            classSpec.addField(FieldSpec.builder(TypeName.BOOLEAN, flagName)
                    .addModifiers(Modifier.PRIVATE)
                    .initializer("false")
                    .build());

            classSpec.addMethod(MethodSpec
                    .methodBuilder(fieldRecipe.fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(diffClass)
                    .addParameter(ParameterSpec.builder(fieldRecipe.fieldType, fieldRecipe.fieldName).build())
                    .addStatement("$L = $L", varName, fieldRecipe.fieldName)
                    .addStatement("$L = true", flagName)
                    .addStatement("return this")
                    .build());
        }

        // apply method
        MethodSpec.Builder applyMethod = MethodSpec
                .methodBuilder("apply")
                .returns(entityInterface)
                .addParameter(ParameterSpec
                        .builder(entityInterface, "source")
                        .addAnnotation(NonNull.class)
                        .build())
                .addCode("return new $T(source.id()", entityImplClass);

        for (FieldRecipe fieldRecipe : entityRecipe.otherFieldRecipes) {
            applyMethod.addCode("\n,($L) ? $L : source.$L()",
                    FLAG_NAME_PREFIX + fieldRecipe.fieldName,
                    VAR_NAME_PREFIX + fieldRecipe.fieldName,
                    fieldRecipe.fieldName);
        }
        applyMethod.addCode(");\n");
        classSpec.addMethod(applyMethod.build());

        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

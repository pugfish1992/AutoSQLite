package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.core.Entity;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by daichi on 12/3/17.
 */

class InterfaceWriter {

    static void write(EntityRecipe entityRecipe, ClassName entityInterface, String packageName, Filer filer)
            throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .interfaceBuilder(entityInterface)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(Entity.class);

        // Define accessor methods
        for (FieldRecipe fieldRecipe : entityRecipe.getOtherFieldRecipes()) {
            classSpec.addMethod(MethodSpec.methodBuilder(fieldRecipe.getFieldName())
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(fieldRecipe.getFieldType())
                    .build());
        }

        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

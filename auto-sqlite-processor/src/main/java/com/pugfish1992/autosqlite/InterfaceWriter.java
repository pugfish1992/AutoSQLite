package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.core.Entity;
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

    static void write(EntityInfo entityInfo, String packageName, Filer filer)
            throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .interfaceBuilder(entityInfo.entityInterface)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(Entity.class);

        // Define accessor methods
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            classSpec.addMethod(MethodSpec.methodBuilder(fieldInfo.fieldName)
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(fieldInfo.fieldType)
                    .build());
        }

        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

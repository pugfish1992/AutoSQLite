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

public class DiffClassWriter {
    private static final String VAR_NAME_PREFIX = "m_";
    private static final String FLAG_NAME_PREFIX = "mSet_";

    static void write(EntityInfo entityInfo, String packageName, Filer filer)
            throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(entityInfo.diffClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        // variables & setters
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            String varName = VAR_NAME_PREFIX + fieldInfo.fieldName;
            String flagName = FLAG_NAME_PREFIX + fieldInfo.fieldName;

            classSpec.addField(FieldSpec.builder(fieldInfo.fieldType, varName)
                    .addModifiers(Modifier.PRIVATE).build());

            classSpec.addField(FieldSpec.builder(TypeName.BOOLEAN, flagName)
                    .addModifiers(Modifier.PRIVATE)
                    .initializer("false")
                    .build());

            classSpec.addMethod(MethodSpec
                    .methodBuilder(fieldInfo.fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(entityInfo.diffClass)
                    .addParameter(ParameterSpec.builder(fieldInfo.fieldType, fieldInfo.fieldName).build())
                    .addStatement("$L = $L", varName, fieldInfo.fieldName)
                    .addStatement("$L = true", flagName)
                    .addStatement("return this")
                    .build());
        }

        // apply method
        MethodSpec.Builder applyMethod = MethodSpec
                .methodBuilder("apply")
                .returns(entityInfo.entityInterface)
                .addParameter(ParameterSpec
                        .builder(entityInfo.entityInterface, "source")
                        .addAnnotation(NonNull.class)
                        .build())
                .addCode("return new $T(source.id()", entityInfo.entityImplClass);

        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            applyMethod.addCode("\n,($L) ? $L : source.$L()",
                    FLAG_NAME_PREFIX + fieldInfo.fieldName,
                    VAR_NAME_PREFIX + fieldInfo.fieldName,
                    fieldInfo.fieldName);
        }
        applyMethod.addCode(");\n");
        classSpec.addMethod(applyMethod.build());

        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

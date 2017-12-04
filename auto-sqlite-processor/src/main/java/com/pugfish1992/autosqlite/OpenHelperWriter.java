package com.pugfish1992.autosqlite;


import com.pugfish1992.autosqlite.core.Entity;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by daichi on 12/3/17.
 */

class OpenHelperWriter {

    private static final String VAR_CURRENT_DB_VERSION = "CURRENT_DB_VERSION";
    private static final String VAR_DB_NAME = "DB_NAME";

    static void write(int currentVersion, VersionedDatabaseRecipes versionedDatabaseRecipes, ClassName openHelperClass,
                      String packageName, Filer filer) throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(openHelperClass)
                .addModifiers(Modifier.FINAL)
                .superclass(Types.SQLITE_OPEN_HELPER);

//        private static final String DB_NAME = "xxx.db";

        classSpec.addField(FieldSpec
                .builder(Types.STRING, VAR_DB_NAME)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", versionedDatabaseRecipes.getDatabaseName())
                .build());

//        private static final int CURRENT_DB_VERSION = ...;

        classSpec.addField(FieldSpec
                .builder(TypeName.INT, VAR_CURRENT_DB_VERSION)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", currentVersion)
                .build()
        );

//        XXXOpenHelper(Context context) {
//            super(context, DB_NAME, null, CURRENT_DB_VERSION);
//        }

        classSpec.addMethod(MethodSpec
        .constructorBuilder()
        .addParameter(Types.CONTEXT, "context")
        .addStatement("super(context, $L, null, $L)", VAR_DB_NAME, VAR_CURRENT_DB_VERSION)
        .build());

//        @Override
//        public void onCreate(SQLiteDatabase db) {...}

        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(Types.SQLITE_DATABASE, "db").build());

        classSpec.addMethod(methodSpec.build());

//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {...}

        methodSpec = MethodSpec
                .methodBuilder("onUpgrade")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(Types.SQLITE_DATABASE, "db").build())
                .addParameter(ParameterSpec.builder(TypeName.INT, "oldVersion").build())
                .addParameter(ParameterSpec.builder(TypeName.INT, "newVersion").build());

        classSpec.addMethod(methodSpec.build());

//        @Override
//        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {...}

        methodSpec = MethodSpec
                .methodBuilder("onDowngrade")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(Types.SQLITE_DATABASE, "db").build())
                .addParameter(ParameterSpec.builder(TypeName.INT, "oldVersion").build())
                .addParameter(ParameterSpec.builder(TypeName.INT, "newVersion").build());

        classSpec.addMethod(methodSpec.build());

        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

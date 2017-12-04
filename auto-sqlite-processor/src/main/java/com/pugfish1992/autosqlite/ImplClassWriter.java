package com.pugfish1992.autosqlite;

import com.squareup.javapoet.ClassName;
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

    static void write(EntityRecipe entityRecipe, ClassName entityImplClass,
                      ClassName entityInterface, String packageName, Filer filer) throws IOException, RuntimeException {

        List<FieldRecipe> fieldRecipes = new ArrayList<>();
        fieldRecipes.add(entityRecipe.pkFieldRecipe);
        fieldRecipes.addAll(entityRecipe.otherFieldRecipes);

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(entityImplClass)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(entityInterface);

        // Declare immutable fields
        for (FieldRecipe fieldRecipe : fieldRecipes) {
            classSpec.addField(FieldSpec
                    .builder(fieldRecipe.fieldType, VARIABLE_NAME_PREFIX + fieldRecipe.fieldName)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build());
        }

        // Define a protected all-args constructor
        MethodSpec.Builder method = MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED);
        for (FieldRecipe fieldRecipe : fieldRecipes) {
            method.addParameter(ParameterSpec.builder(fieldRecipe.fieldType, fieldRecipe.fieldName).build());
            method.addStatement("$L = $L", VARIABLE_NAME_PREFIX + fieldRecipe.fieldName, fieldRecipe.fieldName);
        }
        classSpec.addMethod(method.build());

        // Override id() method
        classSpec.addMethod(MethodSpec.methodBuilder(entityRecipe.pkFieldRecipe.fieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(entityRecipe.pkFieldRecipe.fieldType)
                .addStatement("return $L", VARIABLE_NAME_PREFIX + entityRecipe.pkFieldRecipe.fieldName)
                .build());

        // Override accessor methods
        for (FieldRecipe fieldRecipe : entityRecipe.otherFieldRecipes) {
            classSpec.addMethod(MethodSpec.methodBuilder(fieldRecipe.fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(Override.class)
                    .returns(fieldRecipe.fieldType)
                    .addStatement("return $L", VARIABLE_NAME_PREFIX + fieldRecipe.fieldName)
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
                .addStatement("$T that = ($T) o", entityImplClass, entityImplClass);

        for (int i = 0; i < fieldRecipes.size(); ++i) {
            TypeName type = fieldRecipes.get(i).fieldType;
            String name = VARIABLE_NAME_PREFIX + fieldRecipes.get(i).fieldName;
            boolean isLast = (i + 1 == fieldRecipes.size());

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
                .addCode("$L\n", VARIABLE_NAME_PREFIX + entityRecipe.pkFieldRecipe.fieldName);

        for (FieldRecipe fieldInfo : entityRecipe.otherFieldRecipes) {
            method.addCode(",$L\n", VARIABLE_NAME_PREFIX + fieldInfo.fieldName);
        }

        method.addCode(");\n");
        classSpec.addMethod(method.build());


        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

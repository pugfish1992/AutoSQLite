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

        List<FieldRecipe> fieldRecipesExcludePkField = entityRecipe.getOtherFieldRecipes();
        List<FieldRecipe> allFieldRecipes = entityRecipe.getAllFieldRecipes();
        
        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(entityImplClass)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(entityInterface);

        // Declare immutable fields
        for (FieldRecipe fieldRecipe : allFieldRecipes) {
            classSpec.addField(FieldSpec
                    .builder(fieldRecipe.getFieldType(), VARIABLE_NAME_PREFIX + fieldRecipe.getFieldName())
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build());
        }

        // Define a protected all-args constructor
        MethodSpec.Builder method = MethodSpec.constructorBuilder().addModifiers(Modifier.PROTECTED);
        for (FieldRecipe fieldRecipe : allFieldRecipes) {
            method.addParameter(ParameterSpec.builder(fieldRecipe.getFieldType(), fieldRecipe.getFieldName()).build());
            method.addStatement("$L = $L", VARIABLE_NAME_PREFIX + fieldRecipe.getFieldName(), fieldRecipe.getFieldName());
        }
        classSpec.addMethod(method.build());

        // Override id() method
        classSpec.addMethod(MethodSpec.methodBuilder(entityRecipe.getPkFieldRecipe().getFieldName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(entityRecipe.getPkFieldRecipe().getFieldType())
                .addStatement("return $L", VARIABLE_NAME_PREFIX + entityRecipe.getPkFieldRecipe().getFieldName())
                .build());

        // Override accessor methods
        for (FieldRecipe fieldRecipe : fieldRecipesExcludePkField) {
            classSpec.addMethod(MethodSpec.methodBuilder(fieldRecipe.getFieldName())
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(Override.class)
                    .returns(fieldRecipe.getFieldType())
                    .addStatement("return $L", VARIABLE_NAME_PREFIX + fieldRecipe.getFieldName())
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

        for (int i = 0; i < allFieldRecipes.size(); ++i) {
            TypeName type = allFieldRecipes.get(i).getFieldType();
            String name = VARIABLE_NAME_PREFIX + allFieldRecipes.get(i).getFieldName();
            boolean isLast = (i + 1 == allFieldRecipes.size());

            if (isShortType(type) ||isIntType(type) || isLongType(type) || isBooleanType(type) || isByteType(type)) {
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
                .addCode("$L\n", VARIABLE_NAME_PREFIX + entityRecipe.getPkFieldRecipe().getFieldName());

        for (FieldRecipe fieldInfo : fieldRecipesExcludePkField) {
            method.addCode(",$L\n", VARIABLE_NAME_PREFIX + fieldInfo.getFieldName());
        }

        method.addCode(");\n");
        classSpec.addMethod(method.build());


        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

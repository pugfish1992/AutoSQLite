package com.pugfish1992.autosqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.pugfish1992.autosqlite.core.Entity;
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

import static com.pugfish1992.autosqlite.SupportedTypeUtils.isBooleanType;
import static com.pugfish1992.autosqlite.SupportedTypeUtils.isByteType;
import static com.pugfish1992.autosqlite.SupportedTypeUtils.isDoubleType;
import static com.pugfish1992.autosqlite.SupportedTypeUtils.isFloatType;
import static com.pugfish1992.autosqlite.SupportedTypeUtils.isIntType;
import static com.pugfish1992.autosqlite.SupportedTypeUtils.isLongType;
import static com.pugfish1992.autosqlite.SupportedTypeUtils.isShortType;
import static com.pugfish1992.autosqlite.SupportedTypeUtils.isStringType;

/**
 * Created by daichi on 12/3/17.
 */

class SourceClassWriter {

    private static final String VAR_NULL_OBJECT = "NULL_OBJECT";
    private static final String VAR_OPEN_HELPER = "mOpenHelper";

    static void write(EntityInfo entityInfo, ClassName openHelperClass, String packageName, Filer filer)
            throws IOException, RuntimeException {

        TypeSpec.Builder classSpec = TypeSpec
                .classBuilder(entityInfo.sourceClass)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        final String TABLE_NAME = entityInfo.entityName;

//        private static final NullXXX NULL_OBJECT = new NullXXX();

        classSpec.addField(FieldSpec
                .builder(entityInfo.nullEntityClass, VAR_NULL_OBJECT)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T()", entityInfo.nullEntityClass)
                .build());

//        public static final String CREATE_TABLE = "create if not exists table(id integer primary key, ...)";

        StringBuilder statement = new StringBuilder(
                String.format("create if not exists %s(%s %s primary key",
                TABLE_NAME, entityInfo.primaryKeyField.columnName, entityInfo.primaryKeyField.columnType.toString()));
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            statement.append(String.format(", %s %s", fieldInfo.columnName, fieldInfo.columnType.toString()));
        }
        statement.append(");");
        classSpec.addField(FieldSpec
                .builder(Types.STRING, "CREATE_TABLE")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", statement.toString())
                .build());

//        private final SQLiteOpenHelper mOpenHelper;

        classSpec.addField(FieldSpec
        .builder(Types.SQLITE_OPEN_HELPER, VAR_OPEN_HELPER)
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .build());

//        public XXXSource(@NonNull Context context) {
//            mOpenHelper = new MyOpenHelper(context);
//        }

        classSpec.addMethod(MethodSpec
        .constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(Types.CONTEXT, "context").addAnnotation(NonNull.class).build())
        .addStatement("$L = new $T(context)", VAR_OPEN_HELPER, openHelperClass)
        .build());

//        @NonNull
//        private XXX cursorToEntity(@NonNull Cursor cursor) {
//            return new XXX(...);
//        }

        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("cursorToEntity")
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(NonNull.class)
                .returns(entityInfo.entityInterface)
                .addParameter(ParameterSpec.builder(Types.CURSOR, "cursor").addAnnotation(NonNull.class).build())
                .addCode("return new $T(\n", entityInfo.entityImplClass)
                .addCode("cursor.getLong(cursor.getColumnIndexOrThrow($S))", entityInfo.primaryKeyField.columnName);

        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            if (isBooleanType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getInt(cursor.getColumnIndexOrThrow($S)) != 0", fieldInfo.columnName);
            } else if (isShortType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getShort(cursor.getColumnIndexOrThrow($S))", fieldInfo.columnName);
            } else if (isIntType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getInt(cursor.getColumnIndexOrThrow($S))", fieldInfo.columnName);
            } else if (isLongType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getLong(cursor.getColumnIndexOrThrow($S))", fieldInfo.columnName);
            } else if (isFloatType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getFloat(cursor.getColumnIndexOrThrow($S))", fieldInfo.columnName);
            } else if (isDoubleType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getDouble(cursor.getColumnIndexOrThrow($S))", fieldInfo.columnName);
            } else if (isByteType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getBlob(cursor.getColumnIndexOrThrow($S))[0]", fieldInfo.columnName);
            } else if (isStringType(fieldInfo.fieldType)) {
                methodSpec.addCode(",\ncursor.getString(cursor.getColumnIndexOrThrow($S))", fieldInfo.columnName);
            } else {
                throw new RuntimeException("unsupported type > " + fieldInfo.fieldType.toString());
            }
        }

        methodSpec.addCode(");\n");
        classSpec.addMethod(methodSpec.build());

//        @NonNull
//        private ContentValues entityToContentValues(@NonNull XXX entity, boolean includeID) {
//            ContentValues values = new ContentValues();
//            if(includeID) values.put("_id", entity.id());
//            ...
//            return values;
//        }

        methodSpec = MethodSpec
                .methodBuilder("entityToContentValues")
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(NonNull.class)
                .returns(Types.CONTENT_VALUES)
                .addParameter(ParameterSpec.builder(entityInfo.entityInterface, "entity").addAnnotation(NonNull.class).build())
                .addParameter(ParameterSpec.builder(TypeName.BOOLEAN, "includeID").build())
                .addStatement("$T values = new $T()", Types.CONTENT_VALUES, Types.CONTENT_VALUES)
                .addStatement("if(includeID) values.put($S, entity.$L())", entityInfo.primaryKeyField.columnName, entityInfo.primaryKeyField.fieldName);

        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            if (isBooleanType(fieldInfo.fieldType)) {
                methodSpec.addStatement("values.put($S, entity.$L() ? 1 : 0)", fieldInfo.columnName, fieldInfo.fieldName);
            } else {
                methodSpec.addStatement("values.put($S, entity.$L())", fieldInfo.columnName, fieldInfo.fieldName);
            }
        }

        methodSpec.addStatement("return values");
        classSpec.addMethod(methodSpec.build());

//        @NonNull
//        public XXX load(long id) {
//            SQLiteDatabase db = openReadableDatabase();
//            XXX comment = this.load(id, db);
//            db.close();
//            return comment;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("load")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(NonNull.class)
                .addParameter(ParameterSpec.builder(TypeName.LONG, "id").build())
                .returns(entityInfo.entityInterface)
                .addStatement("$T db = $L.getReadableDatabase()", Types.SQLITE_DATABASE, VAR_OPEN_HELPER)
                .addStatement("$T entity = this.load(id, db)", entityInfo.entityInterface)
                .addStatement("db.close()")
                .addStatement("return entity")
                .build());

//        @NonNull
//        XXX load(long id, @NonNull SQLiteDatabase db) {
//            String selection = "_id" + "=" + String.valueOf(id);
//            Cursor cursor = db.query("xxx", null, selection, null, null, null, null);
//            XXX entity;
//            if (cursor.getCount() == 1) {
//                entity = cursorToEntity(cursor);
//            } else {
//                entity = NULL_OBJECT;
//            }
//            cursor.close();
//            return entity;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("load")
                .addAnnotation(NonNull.class)
                .returns(entityInfo.entityInterface)
                .addParameter(ParameterSpec.builder(TypeName.LONG, "id").build())
                .addParameter(ParameterSpec.builder(Types.SQLITE_DATABASE, "db").addAnnotation(NonNull.class).build())
                .addStatement("$T selection = \"$L = \" + $T.valueOf(id)", Types.STRING, entityInfo.primaryKeyField.columnName, Types.STRING)
                .addStatement("$T cursor = db.query($S, null, selection, null, null, null, null)", Types.CURSOR, TABLE_NAME)
                .addStatement("$T entity", entityInfo.entityInterface)
                .beginControlFlow("if (cursor.getCount() == 1)")
                .addStatement("entity = cursorToEntity(cursor)")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("entity = $L", VAR_NULL_OBJECT)
                .endControlFlow()
                .addStatement("cursor.close()")
                .addStatement("return entity")
                .build());

//        @NonNull
//        public XXX alter(@NonNull XXX entity, @NonNull XXXDiff diff) {
//            SQLiteDatabase db = openWritableDatabase();
//            XXX entity = this.alter(entity, diff, db);
//            db.close();
//            return entity;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("alter")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(NonNull.class)
                .returns(entityInfo.entityInterface)
                .addParameter(ParameterSpec.builder(entityInfo.entityInterface, "entity").addAnnotation(NonNull.class).build())
                .addParameter(ParameterSpec.builder(entityInfo.diffClass, "diff").addAnnotation(NonNull.class).build())
                .addStatement("$T db = $L.getWritableDatabase()", Types.SQLITE_DATABASE, VAR_OPEN_HELPER)
                .addStatement("entity = this.alter(entity, diff, db)")
                .addStatement("db.close()")
                .addStatement("return entity")
                .build());

//        @NonNull
//        XXX alter(@NonNull XXX entity, @NonNull XXXDiff diff, @NonNull SQLiteDatabase db) {
//            if (!entity.isEnable()) return entity;
//            XXX newEntity = diff.apply(entity);
//            String selection = "_id =" + String.valueOf(entity.id());
//            ContentValues values = entityToContentValues(newEntity, true);
//            boolean wasSuccessful;
//            db.beginTransaction();
//            try {
//                int affected = db.update("xxx", values, selection, null);
//                wasSuccessful = (affected == 1);
//                if (wasSuccessful) db.setTransactionSuccessful();
//            } finally {
//                db.endTransaction();
//            }
//            return (wasSuccessful) ? newEntity : entity;
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("alter")
                .addAnnotation(NonNull.class)
                .returns(entityInfo.entityInterface)
                .addParameter(ParameterSpec.builder(entityInfo.entityInterface, "entity").addAnnotation(NonNull.class).build())
                .addParameter(ParameterSpec.builder(entityInfo.diffClass, "diff").addAnnotation(NonNull.class).build())
                .addParameter(ParameterSpec.builder(Types.SQLITE_DATABASE, "db").addAnnotation(NonNull.class).build())
                .addStatement("if (!entity.isEnable()) return entity")
                .addStatement("$T newEntity = diff.apply(entity)", entityInfo.entityInterface)
                .addStatement("$T selection = \"$L = \" + $T.valueOf(entity.id())", Types.STRING, entityInfo.primaryKeyField.columnName, Types.STRING)
                .addStatement("$T values = entityToContentValues(newEntity, true)", Types.CONTENT_VALUES)
                .addStatement("$T wasSuccessful", TypeName.BOOLEAN)
                .addStatement("db.beginTransaction()")
                .beginControlFlow("try")
                .addStatement("$T affected = db.update($S, values, selection, null)", TypeName.INT, TABLE_NAME)
                .addStatement("wasSuccessful = (affected == 1)")
                .addStatement("if (wasSuccessful) db.setTransactionSuccessful()")
                .endControlFlow()
                .beginControlFlow("finally")
                .addStatement("db.endTransaction()")
                .endControlFlow()
                .addStatement("return (wasSuccessful) ? newEntity : entity")
                .build());

//        public void delete(@NonNull XXX entity) {
//            SQLiteDatabase db = openWritableDatabase();
//            this.delete(entity, db);
//            db.close();
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(entityInfo.entityInterface, "entity").addAnnotation(NonNull.class).build())
                .addStatement("$T db = $L.getWritableDatabase()", Types.SQLITE_DATABASE, VAR_OPEN_HELPER)
                .addStatement("this.delete(entity, db)")
                .addStatement("db.close()")
                .build());

//        void delete(@NonNull XXX entity, @NonNull SQLiteDatabase db) {
//            if (!entity.isEnable()) return;
//            String selection = "_id = " + entity.id();
//            db.beginTransaction();
//            try {
//                int affected = db.delete("xxx", selection, null);
//                if (affected == 1) db.setTransactionSuccessful();
//            } finally {
//                db.endTransaction();
//            }
//        }

        classSpec.addMethod(MethodSpec
                .methodBuilder("delete")
                .addParameter(ParameterSpec.builder(entityInfo.entityInterface, "entity").addAnnotation(NonNull.class).build())
                .addParameter(ParameterSpec.builder(Types.SQLITE_DATABASE, "db").addAnnotation(NonNull.class).build())
                .addStatement("if (!entity.isEnable()) return")
                .addStatement("$T selection = \"$L = \" + $T.valueOf(entity.id())", Types.STRING, entityInfo.primaryKeyField.columnName, Types.STRING)
                .addStatement("db.beginTransaction()")
                .beginControlFlow("try")
                .addStatement("$T affected = db.delete($S, selection, null)", TypeName.INT, TABLE_NAME)
                .addStatement("if (affected == 1) db.setTransactionSuccessful()")
                .endControlFlow()
                .beginControlFlow("finally")
                .addStatement("db.endTransaction()")
                .endControlFlow()
                .build());

//        @NonNull
//        public XXX insert(...) {
//            XXX entity = new XXXImpl(Entity.INVALID_ID, ...);
//            ContentValues values = entityToContentValues(entity, false);
//            SQLiteDatabase db = openWritableDatabase();
//            long newRowId = db.insert("xxx", null, values);
//            db.close();
//            if (newRowId != -1) {
//                return new XXXImpl(newRowId, ...);
//            } else {
//                return NULL_OBJECT;
//            }
//        }

        methodSpec = MethodSpec
                .methodBuilder("insert")
                .addAnnotation(NonNull.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(entityInfo.entityInterface);
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            methodSpec.addParameter(ParameterSpec.builder(fieldInfo.fieldType, fieldInfo.fieldName).build());
        }

        methodSpec.addCode("$T entity = new $T($T.INVALID_ID", entityInfo.entityInterface, entityInfo.entityImplClass, Types.ENTITY);
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            methodSpec.addCode(",\n$L", fieldInfo.fieldName);
        }

        methodSpec
                .addCode(");\n")
                .addStatement("$T values = entityToContentValues(entity, false)", Types.CONTENT_VALUES)
                .addStatement("$T db = $L.getWritableDatabase()", Types.SQLITE_DATABASE, VAR_OPEN_HELPER)
                .addStatement("$T newRowId = db.insert($S, null, values)", TypeName.LONG, TABLE_NAME)
                .addStatement("db.close()")
                .beginControlFlow("if(newRowId != -1)")
                .addCode("return new $T(newRowId", entityInfo.entityImplClass);
        for (FieldInfo fieldInfo : entityInfo.otherFields) {
            methodSpec.addCode(",\nentity.$L()", fieldInfo.fieldName);
        }

        methodSpec
                .addCode(");\n")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("return $L", VAR_NULL_OBJECT)
                .endControlFlow();

        classSpec.addMethod(methodSpec.build());

        JavaFile.builder(packageName, classSpec.build()).build().writeTo(filer);
    }
}

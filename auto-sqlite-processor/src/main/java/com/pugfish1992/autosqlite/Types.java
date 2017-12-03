package com.pugfish1992.autosqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pugfish1992.autosqlite.core.Entity;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by daichi on 12/3/17.
 */

class Types {

    static final ClassName STRING = ClassName.get(String.class);
    static final TypeName BYTE_ARRAY = ArrayTypeName.of(TypeName.BYTE);
    static final ClassName OBJECT = ClassName.get(Object.class);
    static final ClassName OBJECTS = ClassName.get(Objects.class);
    static final ClassName ARRAYS = ClassName.get(Arrays.class);
    static final ClassName SQLITE_OPEN_HELPER = ClassName.get(SQLiteOpenHelper.class);
    static final ClassName CONTEXT = ClassName.get(Context.class);
    static final ClassName SQLITE_DATABASE = ClassName.get(SQLiteDatabase.class);
    static final ClassName CURSOR = ClassName.get(Cursor.class);
    static final ClassName CONTENT_VALUES = ClassName.get(ContentValues.class);
    static final ClassName ENTITY = ClassName.get(Entity.class);
}

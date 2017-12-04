package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.core.AffinityType;
import com.squareup.javapoet.TypeName;

/**
 * Created by daichi on 12/4/17.
 */


class FieldRecipe {
    private final String mFieldName;
    private final TypeName mFieldType;
    private final String mColumnName;
    private final AffinityType mColumnType;
    private final Object mDefaultValue;

    FieldRecipe(String fieldName, TypeName fieldType, String columnName, AffinityType columnType, Object defaultValue) {
        mFieldName = fieldName;
        mFieldType = fieldType;
        mColumnName = columnName;
        mColumnType = columnType;
        mDefaultValue = defaultValue;
    }

    String getFieldName() {
        return mFieldName;
    }

    TypeName getFieldType() {
        return mFieldType;
    }

    String getColumnName() {
        return mColumnName;
    }

    AffinityType getColumnType() {
        return mColumnType;
    }

    Object getDefaultValue() {
        return mDefaultValue;
    }
}

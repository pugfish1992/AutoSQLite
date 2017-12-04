package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.core.AffinityType;
import com.squareup.javapoet.TypeName;

/**
 * Created by daichi on 12/4/17.
 */


class FieldRecipe {

    final String fieldName;
    final TypeName fieldType;
    final String columnName;
    final AffinityType columnType;
    final Object defaultValue;

    FieldRecipe(String fieldName, TypeName fieldType, String columnName, AffinityType columnType, Object defaultValue) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.columnName = columnName;
        this.columnType = columnType;
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldRecipe that = (FieldRecipe) o;

        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null)
            return false;
        if (fieldType != null ? !fieldType.equals(that.fieldType) : that.fieldType != null)
            return false;
        if (columnName != null ? !columnName.equals(that.columnName) : that.columnName != null)
            return false;
        if (columnType != that.columnType) return false;
        return defaultValue != null ? defaultValue.equals(that.defaultValue) : that.defaultValue == null;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
        result = 31 * result + (columnName != null ? columnName.hashCode() : 0);
        result = 31 * result + (columnType != null ? columnType.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }
}

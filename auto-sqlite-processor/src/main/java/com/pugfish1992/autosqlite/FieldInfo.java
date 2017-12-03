package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.core.AffinityType;
import com.squareup.javapoet.TypeName;

/**
 * Created by daichi on 12/3/17.
 */

class FieldInfo {
    String fieldName;
    TypeName fieldType;
    String columnName;
    AffinityType columnType;
    Object defaultValue;
}

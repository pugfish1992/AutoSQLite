package com.pugfish1992.autosqlite;

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
}

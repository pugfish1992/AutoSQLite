package com.pugfish1992.autosqlite;

import com.squareup.javapoet.ClassName;

import java.util.List;

/**
 * Created by daichi on 12/3/17.
 */

class EntityInfo {
    String entityName;
    ClassName entityInterface;
    ClassName entityImplClass;
    ClassName nullEntityClass;
    ClassName diffClass;
    ClassName tableClass;
    FieldInfo primaryKeyField;
    List<FieldInfo> otherFields;
}

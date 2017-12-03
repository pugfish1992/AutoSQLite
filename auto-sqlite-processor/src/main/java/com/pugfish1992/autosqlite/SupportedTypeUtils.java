package com.pugfish1992.autosqlite;

import com.pugfish1992.autosqlite.core.AffinityType;
import com.squareup.javapoet.TypeName;

import static com.pugfish1992.autosqlite.Types.STRING;

/**
 * Created by daichi on 12/3/17.
 */

class SupportedTypeUtils {

    static boolean isSupportedJavaType(TypeName typeName) {
        return isBooleanType(typeName) ||
                isShortType(typeName) ||
                isIntType(typeName) ||
                isLongType(typeName) ||
                isFloatType(typeName) ||
                isDoubleType(typeName) ||
                isStringType(typeName) ||
                isByteType(typeName);
    }

    static AffinityType affinityTypeFromSupportedJavaType(TypeName typeName) {
        if (isShortType(typeName) ||
                isIntType(typeName)||
                isLongType(typeName) ||
                isBooleanType(typeName)) {
            return AffinityType.INTEGER;

        } else if (isFloatType(typeName) ||
                isDoubleType(typeName)) {
            return AffinityType.REAL;

        } else if (typeName.equals(STRING)) {
            return AffinityType.TEXT;

        } else {
            return AffinityType.BLOB;
        }
    }

    static boolean isBooleanType(TypeName typeName) {
        return TypeName.BOOLEAN.equals(typeName);
    }

    static boolean isShortType(TypeName typeName) {
        return TypeName.SHORT.equals(typeName);
    }

    static boolean isIntType(TypeName typeName) {
        return TypeName.INT.equals(typeName);
    }

    static boolean isLongType(TypeName typeName) {
        return TypeName.LONG.equals(typeName);
    }

    static boolean isStringType(TypeName typeName) {
        return STRING.equals(typeName);
    }

    static boolean isFloatType(TypeName typeName) {
        return TypeName.FLOAT.equals(typeName);
    }

    static boolean isDoubleType(TypeName typeName) {
        return TypeName.DOUBLE.equals(typeName);
    }

    static boolean isByteType(TypeName typeName) {
        return TypeName.BYTE.equals(typeName);
    }

    static boolean defaultBoolean() {
        return false;
    }

    static short defaultShort() {
        return 0;
    }

    static int defaultInt() {
        return 0;
    }

    static long defaultLong() {
        return 0L;
    }

    static String defaultString() {
        return null;
    }

    static float defaultFloat() {
        return 0f;
    }

    static double defaultDouble() {
        return 0d;
    }

    static byte defaultByte() {
        return 0;
    }

    static Object defaultValueOf(TypeName typeName) {
        if (isBooleanType(typeName)) {
            return defaultBoolean();
        } else if (isShortType(typeName)) {
            return defaultShort();
        } else if (isIntType(typeName)) {
            return defaultInt();
        } else if (isLongType(typeName)) {
            return defaultLong();
        } else if (isStringType(typeName)) {
            return defaultString();
        } else if (isFloatType(typeName)) {
            return defaultFloat();
        } else if (isDoubleType(typeName)) {
            return defaultDouble();
        } else if (isByteType(typeName)) {
            return defaultByte();
        } else {
            return null;
        }
    }
}
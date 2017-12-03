package com.pugfish1992.autosqlite;

/**
 * Created by daichi on 12/3/17.
 */

class Literal {

    static String ofByte(byte value) {
        return String.valueOf(value);
    }

    static String ofByteArray(byte[] value) {
        if (value == null) return "null";
        StringBuilder literal = new StringBuilder("new byte[] {");
        for (int i = 0; i < value.length; ++i) {
            literal.append(ofByte(value[i]));
            if (i + 1 < value.length) literal.append(",");
        }
        literal.append("}");
        return literal.toString();
    }

    static String ofBoolean(boolean value) {
        return String.valueOf(value);
    }

    static String ofShort(short value) {
        return String.valueOf(value);
    }

    static String ofInt(int value) {
        return String.valueOf(value);
    }

    static String ofLong(long value) {
        return String.valueOf(value) + "L";
    }

    static String ofFloat(float value) {
        return String.valueOf(value) + "f";
    }

    static String ofDouble(double value) {
        return String.valueOf(value) + "d";
    }

    static String ofString(String value) {
        return (value == null)
                ? "null"
                : "\"" + value + "\"";
    }

    static String of(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof Byte) {
            return ofByte((Byte) value);
        } else if (value instanceof byte[]) {
            return ofByteArray((byte[]) value);
        } else if (value instanceof Boolean) {
            return ofBoolean((Boolean) value);
        } else if (value instanceof Short) {
            return ofShort((Short) value);
        } else if (value instanceof Integer) {
            return ofInt((Integer) value);
        } else if (value instanceof Long) {
            return ofLong((Long) value);
        } else if (value instanceof Float) {
            return ofFloat((Float) value);
        } else if (value instanceof Double) {
            return ofDouble((Double) value);
        } else if (value instanceof String) {
            return ofString((String) value);
        } else {
            throw new IllegalArgumentException("unsupported type > " + value.getClass().getName());
        }
    }
}

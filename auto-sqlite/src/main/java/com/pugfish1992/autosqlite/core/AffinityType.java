package com.pugfish1992.autosqlite.core;

/**
 * This class is based on the chapter 3 of this page -> https://sqlite.org/datatype3.html
 */
public enum AffinityType {
    TEXT,
    NUMERIC,
    INTEGER,
    REAL,
    BLOB;

    @Override
    public String toString() {
        return this.name();
    }
}
package com.pugfish1992.autosqlite.core;

/**
 * Created by daichi on 12/3/17.
 */

abstract public class NullEntityImpl implements Entity {

    @Override
    public final long id() {
        return INVALID_ID;
    }

    @Override
    public final boolean isEnable() {
        return false;
    }

    @Override
    public final boolean isNull() {
        return true;
    }
}

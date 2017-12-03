package com.pugfish1992.autosqlite.core;

/**
 * Created by daichi on 12/3/17.
 */

abstract public class EntityImpl implements Entity {

    private boolean mIsEnable = true;

    @Override
    public final boolean isEnable() {
        return mIsEnable;
    }

    @Override
    public final boolean isNull() {
        return false;
    }

    protected final void disable() {
        mIsEnable = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityImpl entity = (EntityImpl) o;

        return mIsEnable == entity.mIsEnable;
    }

    @Override
    public int hashCode() {
        return (mIsEnable ? 1 : 0);
    }
}

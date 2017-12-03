package com.pugfish1992.autosqlite.core;

import android.provider.BaseColumns;

/**
 * Created by daichi on 12/3/17.
 */

public interface Entity {

    String PRIMARY_KEY_COLUMN = BaseColumns._ID;
    long INVALID_ID = -1;

    long id();
    boolean isEnable();
    boolean isNull();
}
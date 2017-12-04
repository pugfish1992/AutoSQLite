package com.pugfish1992.sample;

import com.pugfish1992.autosqlite.annotation.CurrentVersion;
import com.pugfish1992.autosqlite.annotation.Database;
import com.pugfish1992.autosqlite.annotation.Entity;
import com.pugfish1992.autosqlite.annotation.Field;

/**
 * Created by daichi on 12/4/17.
 */

@CurrentVersion
@Database(name = "AutoSQLite.db", version = 2)
public class DatabaseContractV2 {

    @Entity("post")
    class PostContract {

        @Field("disliked_count")
        short dislikedCount;

        @Field("is_archived")
        boolean isArchived;
    }

    @Entity("letter")
    class LetterContract {

        @Field("size")
        int size;
    }
}

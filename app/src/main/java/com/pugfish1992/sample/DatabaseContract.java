package com.pugfish1992.sample;

import com.pugfish1992.autosqlite.annotation.*;

/**
 * Created by daichi on 12/3/17.
 */

@Database(name = "AutoSQLite.db", version = 1)
public class DatabaseContract {

    @Entity("post")
    class PostContract {

        @Column("disliked_count")
        short dislikedCount;

        @Column("liked_count")
        int likedCount;

        @Column("view_count")
        long viewCount;

        @Column("ratio")
        float ratio;

        @Column("pi")
        double pi;

        @Column("is_archived")
        boolean isArchived;

        @Column("text")
        String text;

        @Column("byte_code")
        final byte byteCode = 1;

        @Column("byte_array")
        byte[] byteArray;
    }
}

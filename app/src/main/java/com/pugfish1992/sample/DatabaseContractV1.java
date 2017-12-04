package com.pugfish1992.sample;

import com.pugfish1992.autosqlite.annotation.*;

/**
 * Created by daichi on 12/3/17.
 */

@Database(name = "AutoSQLite.db", version = 1)
public class DatabaseContractV1 {

    @Entity("post")
    class PostContract {

        @Field("disliked_count")
        short dislikedCount;

        @Field("liked_count")
        int likedCount;

        @Field("view_count")
        long viewCount;

        @Field("ratio")
        float ratio;

        @Field("pi")
        double pi;

        @Field("is_archived")
        boolean isArchived;

        @Field("text")
        String text;

        @Field("byte_code")
        final byte byteCode = 1;
    }
}

package com.pugfish1992.autosqlite;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daichi on 12/5/17.
 */

class VersionedDatabaseRecipes {

    private final String mDatabaseName;
    private final Map<Integer, DatabaseRecipe> mDatabaseRecipesWithVersion;
    private int mCurrentVersion;

    VersionedDatabaseRecipes(String databaseName) {
        mDatabaseName = databaseName;
        mDatabaseRecipesWithVersion = new HashMap<>();
    }

    void setCurrentVersion(int currentVersion) {
        mCurrentVersion = currentVersion;
    }



    void addDatabaseRecipe(DatabaseRecipe databaseRecipe) {
        if (!databaseRecipe.getName().equals(mDatabaseName)) {
            throw new IllegalArgumentException("invalid db name");
        }
        mDatabaseRecipesWithVersion.put(databaseRecipe.getVersion(), databaseRecipe);
    }

    List<DatabaseRecipe> getSortedDatabaseRecipesByVersion() {
        List<DatabaseRecipe> databaseRecipes = new ArrayList<>(mDatabaseRecipesWithVersion.values());
        Collections.sort(databaseRecipes, new Comparator<DatabaseRecipe>() {
            @Override
            public int compare(DatabaseRecipe databaseRecipe, DatabaseRecipe t1) {
                return Integer.valueOf(databaseRecipe.getVersion()).compareTo(t1.getVersion());
            }
        });
        return databaseRecipes;
    }

    DatabaseRecipe findDatabaseRecipeByVersion(int version) {
        return mDatabaseRecipesWithVersion.get(version);
    }

    DatabaseRecipe findCurrentVersionDatabaseRecipe() {
        return findDatabaseRecipeByVersion(mCurrentVersion);
    }

    public int getCurrentVersion() {
        return mCurrentVersion;
    }

    public String getDatabaseName() {
        return mDatabaseName;
    }
}

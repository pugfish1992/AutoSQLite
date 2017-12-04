package com.pugfish1992.autosqlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by daichi on 12/4/17.
 */

class DatabaseRecipe {

    private final String mName;
    private final int mVersion;
    private final List<EntityRecipe> mEntityRecipes;

    DatabaseRecipe(String name, int version) {
        mName = name;
        mVersion = version;
        mEntityRecipes = new ArrayList<>();
    }

    void addEntityRecipe(EntityRecipe entityRecipe) {
        mEntityRecipes.add(entityRecipe);
    }

    void addEntityRecipes(List<EntityRecipe> entityRecipes) {
        mEntityRecipes.addAll(entityRecipes);
    }

    List<EntityRecipe> getEntityRecipes() {
        return Collections.unmodifiableList(mEntityRecipes);
    }

    String getName() {
        return mName;
    }

    int getVersion() {
        return mVersion;
    }
}

package com.pugfish1992.autosqlite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by daichi on 12/4/17.
 */

class EntityRecipe {

    private final String mName;
    private final FieldRecipe mPkFieldRecipe;
    private final List<FieldRecipe> mOtherFieldRecipes;

    EntityRecipe(String name, FieldRecipe pkFieldRecipe) {
        this.mName = name;
        this.mPkFieldRecipe = pkFieldRecipe;
        this.mOtherFieldRecipes = new ArrayList<>();
    }

    void addFieldRecipe(FieldRecipe fieldRecipe) {
        mOtherFieldRecipes.add(fieldRecipe);
    }

    void addFieldRecipes(List<FieldRecipe> fieldRecipes) {
        mOtherFieldRecipes.addAll(fieldRecipes);
    }

    String getName() {
        return mName;
    }

    FieldRecipe getPkFieldRecipe() {
        return mPkFieldRecipe;
    }

    List<FieldRecipe> getAllFieldRecipes() {
        List<FieldRecipe> all = new ArrayList<>();
        all.add(mPkFieldRecipe);
        all.addAll(mOtherFieldRecipes);
        return Collections.unmodifiableList(all);
    }

    List<FieldRecipe> getOtherFieldRecipes() {
        return Collections.unmodifiableList(mOtherFieldRecipes);
    }
}

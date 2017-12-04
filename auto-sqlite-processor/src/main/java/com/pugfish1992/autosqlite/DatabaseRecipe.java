package com.pugfish1992.autosqlite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by daichi on 12/4/17.
 */

class DatabaseRecipe {

    final String name;
    final Map<Integer, Set<EntityRecipe>> versionedEntityRecipeSets;

    DatabaseRecipe(String name) {
        this.name = name;
        versionedEntityRecipeSets = new HashMap<>();
    }

    void addEntityRecipeWithVersion(EntityRecipe entityRecipe, int version) {
        if (!versionedEntityRecipeSets.containsKey(version)) {
            versionedEntityRecipeSets.put(version, new HashSet<EntityRecipe>());
        }
        versionedEntityRecipeSets.get(version).add(entityRecipe);
    }

    Set<EntityRecipe> findEntityRecipeSetOfVersion(int version) {
        return versionedEntityRecipeSets.get(version);
    }
}

package com.pugfish1992.autosqlite;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by daichi on 12/4/17.
 */

class DatabaseRecipe {

    final String name;
    final int version;
    final Set<EntityRecipe> entityRecipes;

    DatabaseRecipe(String name, int version) {
        this.name = name;
        this.version = version;
        entityRecipes = new HashSet<>();
    }

    void addEntityRecipe(EntityRecipe entityRecipe) {
        entityRecipes.add(entityRecipe);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseRecipe that = (DatabaseRecipe) o;

        if (version != that.version) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return entityRecipes != null ? entityRecipes.equals(that.entityRecipes) : that.entityRecipes == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + version;
        result = 31 * result + (entityRecipes != null ? entityRecipes.hashCode() : 0);
        return result;
    }
}

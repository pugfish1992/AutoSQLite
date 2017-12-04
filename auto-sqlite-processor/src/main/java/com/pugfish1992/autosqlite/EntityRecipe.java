package com.pugfish1992.autosqlite;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by daichi on 12/4/17.
 */

class EntityRecipe {

    final String name;
    final FieldRecipe pkFieldRecipe;
    final Set<FieldRecipe> otherFieldRecipes;

    EntityRecipe(String name, FieldRecipe pkFieldRecipe) {
        this.name = name;
        this.pkFieldRecipe = pkFieldRecipe;
        this.otherFieldRecipes = new HashSet<>();
    }

    void addFieldRecipe(FieldRecipe fieldRecipe) {
        otherFieldRecipes.add(fieldRecipe);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityRecipe that = (EntityRecipe) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (pkFieldRecipe != null ? !pkFieldRecipe.equals(that.pkFieldRecipe) : that.pkFieldRecipe != null)
            return false;
        return otherFieldRecipes != null ? otherFieldRecipes.equals(that.otherFieldRecipes) : that.otherFieldRecipes == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (pkFieldRecipe != null ? pkFieldRecipe.hashCode() : 0);
        result = 31 * result + (otherFieldRecipes != null ? otherFieldRecipes.hashCode() : 0);
        return result;
    }
}

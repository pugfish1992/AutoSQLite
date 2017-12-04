package com.pugfish1992.autosqlite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daichi on 12/4/17.
 */

class DatabaseDiff {
    List<EntityRecipe> removedEntities = new ArrayList<>();
    List<EntityRecipe> addedEntities = new ArrayList<>();
    List<EntityDiff> changedEntities = new ArrayList<>();
}

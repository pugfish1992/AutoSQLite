package com.pugfish1992.autosqlite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daichi on 12/4/17.
 */

class EntityDiff {
    List<FieldRecipe> removedFields = new ArrayList<>();
    List<FieldRecipe> addedFields = new ArrayList<>();
    List<FieldDiff> changedFields = new ArrayList<>();
}

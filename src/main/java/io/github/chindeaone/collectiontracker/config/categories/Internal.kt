package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose

class Internal {

    /**
     * This flag was added because i had realized at that time that the positions were stored in uppercase.
     * Now i have to migrate them on the first loading.
     */
    @Expose
    var migratedToLowercasePositions: Boolean = true
}
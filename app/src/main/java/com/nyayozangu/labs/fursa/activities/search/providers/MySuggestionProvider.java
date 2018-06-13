package com.nyayozangu.labs.fursa.activities.search.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Sean on 4/16/18.
 */

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY =
            "MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
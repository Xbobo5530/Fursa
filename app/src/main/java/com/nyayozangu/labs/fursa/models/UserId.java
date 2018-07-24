package com.nyayozangu.labs.fursa.models;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

/**
 * Created by Sean on 4/7/18.
 */

public class UserId {

    /**
     * @returns a String: the user_id
     */
    @Exclude
    public String UserId;

    public <T extends UserId> T withId(@NonNull final String id) {
        this.UserId = id;
        return (T) this;
    }
}

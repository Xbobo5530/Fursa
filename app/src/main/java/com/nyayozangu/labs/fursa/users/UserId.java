package com.nyayozangu.labs.fursa.users;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

/**
 * Created by Sean on 4/7/18.
 */

public class UserId {

    /**
     * @returns a String: the userId
     */
    @Exclude
    public String UserId;

    public <T extends UserId> T withId(@NonNull final String id) {
        this.UserId = id;
        return (T) this;
    }
}

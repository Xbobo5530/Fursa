package com.nyayozangu.labs.fursa;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

/**
 * Created by Sean on 4/4/18.
 * an extended class to handle the postId for the likes feature
 */

public class PostId {

    /**
     * @returns a String: the postId
     */
    @Exclude
    public String PostId;

    public <T extends PostId> T withId(@NonNull final String id) {
        this.PostId = id;
        return (T) this;
    }


}

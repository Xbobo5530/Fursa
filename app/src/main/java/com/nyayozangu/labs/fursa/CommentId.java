package com.nyayozangu.labs.fursa;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

/**
 * Created by Sean on 4/9/18.
 * an extended class to handle comment id
 */

class CommentId {

    /**
     * @returns a String: the commentId
     */
    @Exclude
    public String CommentId;

    public <T extends CommentId> T withId(@NonNull final String id) {
        this.CommentId = id;
        return (T) this;
    }

}

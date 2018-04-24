package com.nyayozangu.labs.fursa.activities.comments.models;

import java.util.Date;

/**
 * Created by Sean on 4/9/18.
 * model class for comments
 */

public class Comments extends CommentId {

    //get data from database
    public String comment;
    public String user_id;
    public Date timestamp;

    //empty constructor
    public Comments() {
    }

    public Comments(String comment, String user_id, Date timestamp) {
        this.comment = comment;
        this.timestamp = timestamp;
        this.user_id = user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}

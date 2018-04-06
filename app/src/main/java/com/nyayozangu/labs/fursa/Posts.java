package com.nyayozangu.labs.fursa;

import java.util.Date;
import java.util.List;

/**
 * Created by Sean on 4/4/18.
 * Model class for the posts
 */

public class Posts extends PostId {

    //get all the details from database
    public String user_id, image_url, thumb_url, desc, title;

    public Date timestamp;


    public Posts(Date timestamp) {
        this.timestamp = timestamp;
    }

    //empty constructor
    public Posts() {
    }


    public Posts(String user_id, String image_url, String thumb_url, String desc, String title, Date timestamp, List saves) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.desc = desc;
        this.title = title;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}

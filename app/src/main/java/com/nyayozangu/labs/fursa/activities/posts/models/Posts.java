package com.nyayozangu.labs.fursa.activities.posts.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Sean on 4/4/18.
 * Model class for the posts
 */

public class Posts extends com.nyayozangu.labs.fursa.activities.posts.models.PostId {

    //get all the details from database
    public String user_id, image_url, thumb_url, desc, title, price, image_text, image_labels;
    public ArrayList<String> location, contact_details, categories, tags;
    public Date timestamp, event_date;



    //empty constructor
    public Posts() {
    }

    //main constructor
    public Posts(String user_id, String image_url, String thumb_url, String image_labels,
                 String image_text, String desc, String title, String price,
                 Date timestamp, Date event_date,
                 ArrayList<String> categories, ArrayList<String> contact_details,
                 ArrayList<String> location, ArrayList<String> tags) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.image_labels = image_labels;
        this.image_text = image_text;
        this.desc = desc;
        this.title = title;
        this.price = price;
        this.timestamp = timestamp;
        this.event_date = event_date;
        this.categories = categories;
        this.location = location;
        this.timestamp = timestamp;
        this.contact_details = contact_details;
        this.tags = tags;

    }

    //getters

    //setters
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

    public String getImage_text() {
        return image_text;
    }

    public void setImage_text(String image_text) {
        this.image_text = image_text;
    }

    public String getImage_labels() {
        return image_labels;
    }

    public void setImage_labels(String image_labels) {
        this.image_labels = image_labels;
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

    public Date getEvent_date() {
        return event_date;
    }

    public void setEvent_date(Date event_date) {
        this.event_date = event_date;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public ArrayList<String> getLocation() {
        return location;
    }

    public void setLocation(ArrayList<String> location) {
        this.location = location;
    }

    public ArrayList<String> getContact_details() {
        return contact_details;
    }

    public void setContact_details(ArrayList<String> contact_details) {
        this.contact_details = contact_details;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

}

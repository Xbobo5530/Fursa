package com.nyayozangu.labs.fursa.activities.posts.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Sean on 4/4/18.
 * Model class for the posts
 */

public class Posts extends com.nyayozangu.labs.fursa.activities.posts.models.PostId {

    // TODO: 6/3/18 To hide this warning and ensure your app does not break, you need to add the following code to your app before calling any other Cloud Firestore methods:
    //
    //    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    //    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    //        .setTimestampsInSnapshotsEnabled(true)
    //        .build();
    //    firestore.setFirestoreSettings(settings);
    //
    //    With this change, timestamps stored in Cloud Firestore will be read back as com.google.firebase.Timestamp objects instead of as system java.util.Date objects. So you will also need to update code expecting a java.util.Date to instead expect a Timestamp. For example:
    //
    //    // Old:
    //    java.util.Date date = snapshot.getDate("created_at");
    //    // New:
    //    Timestamp timestamp = snapshot.getTimestamp("created_at");
    //    java.util.Date date = timestamp.toDate();
    //
    //    Please audit all existing usages of java.util.Date when you enable the new behavior. In a future release, the behavior will be changed to the new behavior, so if you do not follow these steps, YOUR APP MAY BREAK.

    //get all the details from database
    public String user_id, image_url, thumb_url, desc, title, price, image_text, image_labels;
    public ArrayList<String> location, contact_details, categories, tags;
    public Date timestamp, event_date;
    public int likes, comments, views;

    //empty constructor
    public Posts() {
    }

    //main constructor
    public Posts(String user_id, String image_url, String thumb_url, String image_labels,
                 String image_text, String desc, String title, String price,
                 Date timestamp, Date event_date,
                 ArrayList<String> categories, ArrayList<String> contact_details,
                 ArrayList<String> location, ArrayList<String> tags, int likes, int comments, int views) {
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
        this.likes = likes;
        this.comments = comments;
        this.views = views;

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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}

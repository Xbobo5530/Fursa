package com.nyayozangu.labs.fursa;

import java.util.Date;

/**
 * Created by Sean on 4/4/18.
 * Model class for the posts
 */

public class Posts extends PostId {

    //get all the details from database
    public String user_id;
    public String image_url;
    public String thumb_url;
    public String desc;
    public String title;
    public String location_name;
    public String location_address;
    public String contact_name;
    public String contact_phone;
    public String contact_email;
    public Date timestamp;
    public Date event_date;

    //empty constructor
    public Posts() {
    }

    public Posts(String user_id, String image_url, String thumb_url, String desc, String title, Date timestamp, Date event_date, String contact_name, String contact_email, String contact_phone) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.desc = desc;
        this.title = title;
        this.timestamp = timestamp;
        this.event_date = event_date;
        this.location_name = location_name;
        this.location_address = location_address;
        this.timestamp = timestamp;
        this.contact_name = contact_name;
        this.contact_phone = contact_phone;
        this.contact_email = contact_email;
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

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getLocation_address() {
        return location_address;
    }

    public void setLocation_address(String location_address) {
        this.location_address = location_address;
    }

    public Date getEvent_date() {
        return event_date;
    }

    public void setEvent_date(Date event_date) {
        this.event_date = event_date;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }
}

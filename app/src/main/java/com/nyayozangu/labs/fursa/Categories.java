package com.nyayozangu.labs.fursa;

import java.util.Date;

/**
 * Created by Sean on 4/18/18.
 */

public class Categories {

    //get items from db

    public String key;
    public String value;
    public String desc;
    public Date timestamp;


    public Categories() {

    }

    public Categories(String key, String value, String desc, Date timestamp) {
        this.key = key;
        this.value = value;
        this.desc = desc;
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

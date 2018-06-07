package com.nyayozangu.labs.fursa.activities.tags;

public class Tags {

    public String title;
    public int post_count;

    public Tags() {
    }

    public Tags(String title, int post_count) {
        this.title = title;
        this.post_count = post_count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPost_count() {
        return post_count;
    }

    public void setPost_count(int post_count) {
        this.post_count = post_count;
    }
}

package com.nyayozangu.labs.fursa;

/**
 * Created by Sean on 4/7/18.
 * model class for users
 */

public class Users extends UserId {

    // TODO: 4/7/18 get date user joined

    public String username;
    public String userId;
    public String userImage;
    public String userThumb;


    public Users(String name, String user_id, String image, String thumb) {
        this.username = name;
        this.userId = user_id;
        this.userImage = image;
        this.userThumb = thumb;
    }

    //empty constructor
    public Users() {
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserThumb() {
        return userThumb;
    }

    public void setUserThumb(String userThumb) {
        this.userThumb = userThumb;
    }

    public String toString() {
        return "username is: " + this.getUsername() +
                "\nuserId is: " + this.getUserId() +
                "\nuserImageUrl is: " + this.getUserImage() +
                "\nuserThumbUrl is: " + this.getUserThumb();
    }
}

package com.nyayozangu.labs.fursa;

/**
 * Created by Sean on 4/7/18.
 * model class for users
 */

public class Users extends UserId {

    // TODO: 4/7/18 get date user joined

    public String username, userId, userImage, userThumb, bio;


    public Users(String name, String user_id, String image, String thumb, String bio) {
        this.username = name;
        this.userId = user_id;
        this.userImage = image;
        this.userThumb = thumb;
        this.bio = bio;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String toString() {
        return "username is: " + this.getUsername() +
                "\nuserId is: " + this.getUserId() +
                "\nuserImageUrl is: " + this.getUserImage() +
                "\nuserThumbUrl is: " + this.getUserThumb();
    }
}

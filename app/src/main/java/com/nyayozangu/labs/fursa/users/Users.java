package com.nyayozangu.labs.fursa.users;

/**
 * Created by Sean on 4/7/18.
 * model class for users
 */

public class Users extends UserId {

    // TODO: 4/7/18 get date user joined

    public String name, user_id, image, thumb, bio;


    public Users(String name, String user_id, String image, String thumb, String bio) {
        this.name = name;
        this.user_id = user_id;
        this.image = image;
        this.thumb = thumb;
        this.bio = bio;
    }

    //empty constructor
    public Users() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String toString() {
        return "name is: " + this.getName() +
                "\nuser_id is: " + this.getUser_id() +
                "\nuserImageUrl is: " + this.getImage() +
                "\nuserThumbUrl is: " + this.getThumb();
    }

}

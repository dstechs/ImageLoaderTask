package imageloader.task.com.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("profile_image")
    private ProfileImage profileImage;

    @SerializedName("name")
    private String name;

    @SerializedName("links")
    private Links links;

    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    public ProfileImage getProfileImage() {
        return profileImage;
    }

    public String getName() {
        return name;
    }

    public Links getLinks() {
        return links;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
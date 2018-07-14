package imageloader.task.com.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DetailModel {

    @SerializedName("urls")
    private Urls urls;

    @SerializedName("current_user_collections")
    private List<Object> currentUserCollections;

    @SerializedName("color")
    private String color;

    @SerializedName("width")
    private int width;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("links")
    private Links links;

    @SerializedName("id")
    private String id;

    @SerializedName("categories")
    private List<CategoriesItem> categories;

    @SerializedName("liked_by_user")
    private boolean likedByUser;

    @SerializedName("user")
    private User user;

    @SerializedName("height")
    private int height;

    @SerializedName("likes")
    private int likes;

    public Urls getUrls() {
        return urls;
    }

    public List<Object> getCurrentUserCollections() {
        return currentUserCollections;
    }

    public String getColor() {
        return color;
    }

    public int getWidth() {
        return width;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Links getLinks() {
        return links;
    }

    public String getId() {
        return id;
    }

    public List<CategoriesItem> getCategories() {
        return categories;
    }

    public boolean isLikedByUser() {
        return likedByUser;
    }

    public User getUser() {
        return user;
    }

    public int getHeight() {
        return height;
    }

    public int getLikes() {
        return likes;
    }
}
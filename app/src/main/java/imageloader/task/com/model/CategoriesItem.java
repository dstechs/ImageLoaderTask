package imageloader.task.com.model;

import com.google.gson.annotations.SerializedName;

public class CategoriesItem {

    @SerializedName("photo_count")
    private int photoCount;

    @SerializedName("links")
    private Links links;

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    public int getPhotoCount() {
        return photoCount;
    }

    public Links getLinks() {
        return links;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
package imageloader.task.com.model;

import com.google.gson.annotations.SerializedName;

public class Urls {

    @SerializedName("small")
    private String small;

    @SerializedName("thumb")
    private String thumb;

    @SerializedName("raw")
    private String raw;

    @SerializedName("regular")
    private String regular;

    @SerializedName("full")
    private String full;

    public String getSmall() {
        return small;
    }

    public String getThumb() {
        return thumb;
    }

    public String getRaw() {
        return raw;
    }

    public String getRegular() {
        return regular;
    }

    public String getFull() {
        return full;
    }
}
package eu.endercentral.crazy_advancements.advancement.serialized;

import com.google.gson.JsonObject;

public class SerializedAdvancementDisplay {

    private final String icon;
    private final JsonObject title;
    private final JsonObject description;
    private final String frame;
    private final String visibility;
    private final String backgroundTexture;
    private final float x;
    private final float y;

    public SerializedAdvancementDisplay(String icon, JsonObject title, JsonObject description, String frame, String visibility, String backgroundTexture, float x, float y) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.frame = frame;
        this.visibility = visibility;
        this.backgroundTexture = backgroundTexture;
        this.x = x;
        this.y = y;
    }

    public String getIcon() {
        return icon;
    }

    public JsonObject getTitle() {
        return title;
    }

    public JsonObject getDescription() {
        return description;
    }

    public String getFrame() {
        return frame;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getBackgroundTexture() {
        return backgroundTexture;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

}

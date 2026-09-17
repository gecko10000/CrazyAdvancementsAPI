package eu.endercentral.crazy_advancements.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class SerializedCustomItem {

    private final String item;
    private final int customModelData;

    public SerializedCustomItem(String item, int customModelData) {
        this.item = item;
        this.customModelData = customModelData;
    }

    public String getItem() {
        return item;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public CustomItem deserialize(NamespacedKey name) {
        Material type = Material.matchMaterial(getItem());
        return new CustomItem(name, type, customModelData);
    }

}

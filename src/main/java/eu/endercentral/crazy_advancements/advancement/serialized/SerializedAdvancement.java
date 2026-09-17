package eu.endercentral.crazy_advancements.advancement.serialized;

import eu.endercentral.crazy_advancements.CrazyAdvancementsAPI;
import eu.endercentral.crazy_advancements.advancement.AdvancementFunctionReward;
import org.bukkit.NamespacedKey;

import javax.annotation.Nullable;
import java.util.List;

public class SerializedAdvancement {

    private final transient NamespacedKey name;
    private final SerializedAdvancementDisplay display;
    private final SerializedCriteria criteria;
    private final AdvancementFunctionReward reward;
    private final String parent;
    private final List<String> flags;

    public SerializedAdvancement(NamespacedKey name, SerializedAdvancementDisplay display, SerializedCriteria criteria, AdvancementFunctionReward reward, String parent, List<String> flags) {
        this.name = name;
        this.display = display;
        this.criteria = criteria;
        this.reward = reward;
        this.parent = parent;
        this.flags = flags;
    }

    public NamespacedKey getName() {
        return name;
    }

    public SerializedAdvancementDisplay getDisplay() {
        return display;
    }

    public SerializedCriteria getCriteria() {
        return criteria;
    }

    public AdvancementFunctionReward getReward() {
        return reward;
    }

    @Nullable
    public String getParent() {
        return parent;
    }

    public List<String> getFlags() {
        return flags;
    }

    public String toJson() {
        return CrazyAdvancementsAPI.getGson().toJson(this);
    }

}

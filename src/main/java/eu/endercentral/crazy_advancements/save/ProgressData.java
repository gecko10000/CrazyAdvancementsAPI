package eu.endercentral.crazy_advancements.save;

import eu.endercentral.crazy_advancements.advancement.criteria.CriteriaType;
import org.bukkit.NamespacedKey;

/**
 * Represents the Save Data for an Advancement saved by {@link CriteriaType} NUMBER
 *
 * @author Axel
 *
 */
public class ProgressData {

    private final NamespacedKey name;
    private final int progress;

    /**
     * Constructor for creating ProgressData
     *
     * @param name     The Unique Name of the Advancement
     * @param progress The Progress
     */
    public ProgressData(NamespacedKey name, int progress) {
        this.name = name;
        this.progress = progress;
    }

    /**
     * Gets the Unique Name of the Advancement
     *
     * @return The Unique Name
     */
    public NamespacedKey getName() {
        return name;
    }

    /**
     * Gets the Progress
     *
     * @return The Progress
     */
    public int getProgress() {
        return progress;
    }

}

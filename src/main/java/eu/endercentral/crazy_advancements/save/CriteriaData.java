package eu.endercentral.crazy_advancements.save;

import eu.endercentral.crazy_advancements.advancement.criteria.CriteriaType;
import org.bukkit.NamespacedKey;

import java.util.List;

/**
 * Represents the Save Data for an Advancement saved by {@link CriteriaType} LIST
 *
 * @author Axel
 *
 */
public class CriteriaData {

    private final NamespacedKey name;
    private final List<String> criteria;

    /**
     * Constructor for creating CriteriaData
     *
     * @param name     The Unique Name of the Advancement
     * @param criteria The Criteria that has been awarded
     */
    public CriteriaData(NamespacedKey name, List<String> criteria) {
        this.name = name;
        this.criteria = criteria;
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
     * Gets the Criteria that has been awarded
     *
     * @return The Criteria
     */
    public List<String> getCriteria() {
        return criteria;
    }

}

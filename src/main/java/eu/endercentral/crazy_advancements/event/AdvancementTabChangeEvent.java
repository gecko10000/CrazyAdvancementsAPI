package eu.endercentral.crazy_advancements.event;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

/**
 * Called when a Player opens their Advancement Screen or changes their Advancement Tab
 *
 * @author Axel
 *
 */
public class AdvancementTabChangeEvent extends Event implements Cancellable {

    public static final HandlerList handlers = new HandlerList();

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }


    private final Player player;
    private NamespacedKey tabAdvancement;
    private boolean cancelled;

    /**
     * Constructor for instantiating this Event
     *
     * @param player         The Player
     * @param tabAdvancement The selected Tab
     */
    public AdvancementTabChangeEvent(Player player, NamespacedKey tabAdvancement) {
        super(true);
        this.player = player;
        this.tabAdvancement = tabAdvancement;
    }

    /**
     * Gets the Player
     *
     * @return The Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the selected Tab
     *
     * @return The selected Tab
     */
    public NamespacedKey getTabAdvancement() {
        return tabAdvancement;
    }

    /**
     * Changes the tab the player is changing to
     *
     * @param tabAdvancement The new tab the player will change to
     */
    public void setTabAdvancement(NamespacedKey tabAdvancement) {
        this.tabAdvancement = tabAdvancement;
    }


    /**
     * Gets an informative String for debugging purposes
     *
     * @return Information about this event
     */
    public String getInformationString() {
        return "tab_action=change;player=" + player.getName() + ";tab=" + tabAdvancement.toString() + ",cancelled=" + cancelled;
    }

}

package eu.endercentral.crazy_advancements.packet;

import eu.endercentral.crazy_advancements.CrazyAdvancementsAPI;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents an Advancements Packet
 *
 * @author Axel
 *
 */
public class AdvancementsPacket {

    private final Player player;
    private final boolean reset;
    private final List<Advancement> advancements;
    private final List<NamespacedKey> removedAdvancements;
    private final boolean showAdvancements;

    /**
     * Constructor for creating Advancement Packets
     *
     * @param player              The target Player
     * @param reset               Whether the Client will clear the Advancement Screen before adding the Advancements
     * @param advancements        A list of advancements that should be added to the Advancement Screen
     * @param removedAdvancements A list of NameKeys which should be removed from the Advancement Screen
     * @param showAdvancements    Whether to show toasts for any completed advancements (must also have showToasts enabled in the advancement itself)
     */
    public AdvancementsPacket(Player player, boolean reset, List<Advancement> advancements, List<NamespacedKey> removedAdvancements, boolean showAdvancements) {
        this.player = player;
        this.reset = reset;
        this.advancements = advancements == null ? new ArrayList<>() : new ArrayList<>(advancements);
        this.removedAdvancements = removedAdvancements == null ? new ArrayList<>() : new ArrayList<>(removedAdvancements);
        this.showAdvancements = showAdvancements;
    }

    /**
     * For backwards compatibility.
     */
    public AdvancementsPacket(Player player, boolean reset, List<Advancement> advancements, List<NamespacedKey> removedAdvancements) {
        this(player, reset, advancements, removedAdvancements, true);
    }

    /**
     * Gets the target Player
     *
     * @return The target Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets whether the Client will clear the Advancement Screen before adding the Advancements
     *
     * @return Whether the Screen should reset
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * Gets a copy of the list of the added Advancements
     *
     * @return The list containing the added Advancements
     */
    public List<Advancement> getAdvancements() {
        return new ArrayList<>(advancements);
    }

    /**
     * Gets a copy of the list of the removed Advancement's NameKeys
     *
     * @return The list containing the removed Advancement's NameKeys
     */
    public List<NamespacedKey> getRemovedAdvancements() {
        return new ArrayList<>(removedAdvancements);
    }

    public boolean isShowAdvancements() {
        return showAdvancements;
    }

    /**
     * Builds a packet that can be sent to a Player
     *
     * @return The Packet
     */
    public ClientboundUpdateAdvancementsPacket build() {
        //Create Lists
        List<ClientboundUpdateAdvancementsPacket.PositionedAdvancement> advancements = new ArrayList<>();
        Set<Identifier> removedAdvancements = new HashSet<>();
        Map<Identifier, AdvancementProgress> progress = new HashMap<>();

        //Populate Lists
        for (Advancement advancement : this.advancements) {
            final Identifier advancementName = CrazyAdvancementsAPI.namespacedKeyToIdentifier(advancement.getName());
            net.minecraft.advancements.Advancement nmsAdvancement = convertAdvancement(advancement);
            final AdvancementHolder holder = new AdvancementHolder(advancementName, nmsAdvancement);
            final AdvancementDisplay display = advancement.getDisplay();
            float x = PacketConverter.generateX(advancement.getTab(), display.generateX());
            float y = PacketConverter.generateY(advancement.getTab(), display.generateY());
            advancements.add(new ClientboundUpdateAdvancementsPacket.PositionedAdvancement(holder, x, y));
            progress.put(advancementName, advancement.getProgress(getPlayer()).getNmsProgress());
        }
        for (NamespacedKey removed : this.removedAdvancements) {
            removedAdvancements.add(CrazyAdvancementsAPI.namespacedKeyToIdentifier(removed));
        }

        //Create Packet
        ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(isReset(), advancements, removedAdvancements, progress, showAdvancements);
        return packet;
    }

    protected net.minecraft.advancements.Advancement convertAdvancement(Advancement advancement) {
        return PacketConverter.toNmsAdvancement(advancement);
    }

    /**
     * Sends the Packet to the target Player
     *
     */
    public void send() {
        ClientboundUpdateAdvancementsPacket packet = build();
        ((CraftPlayer) getPlayer()).getHandle().connection.send(packet, null);
    }


}

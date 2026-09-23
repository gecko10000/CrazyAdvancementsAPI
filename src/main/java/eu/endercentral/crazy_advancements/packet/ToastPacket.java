package eu.endercentral.crazy_advancements.packet;

import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents an Advancements Packet for Toast Notifications
 *
 * @author Axel
 *
 */
public class ToastPacket {

    private final Player player;
    private final boolean add;
    private final ToastNotification notification;

    /**
     * Constructor for creating Toast Packets
     *
     * @param player       The target Player
     * @param add          Whether to add or remove the Advancement
     * @param notification The Notification
     */
    public ToastPacket(Player player, boolean add, ToastNotification notification) {
        this.player = player;
        this.add = add;
        this.notification = notification;
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
     * Gets whether the Advancement is added or removed
     *
     * @return Whether the Advancement is added or removed
     */
    public boolean isAdd() {
        return add;
    }

    /**
     * Gets the Notification
     *
     * @return The Notification
     */
    public ToastNotification getNotification() {
        return notification;
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
        final Identifier notificationNameIdentifier = Identifier.fromNamespaceAndPath(ToastNotification.NOTIFICATION_NAME.namespace(), ToastNotification.NOTIFICATION_NAME.value());

        //Populate Lists
        if (add) {
            final Advancement advancement = PacketConverter.toNmsToastAdvancement(getNotification());
            final AdvancementHolder holder = new AdvancementHolder(notificationNameIdentifier, advancement);
            advancements.add(new ClientboundUpdateAdvancementsPacket.PositionedAdvancement(holder, 0, 0));
            progress.put(notificationNameIdentifier, ToastNotification.NOTIFICATION_PROGRESS.getNmsProgress());
        } else {
            removedAdvancements.add(notificationNameIdentifier);
        }

        //Create Packet
        ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(false, advancements, removedAdvancements, progress, true);
        return packet;
    }

    /**
     * Sends the Packet to the target Player
     *
     */
    public void send() {
        ClientboundUpdateAdvancementsPacket packet = build();
        ((CraftPlayer) getPlayer()).getHandle().connection.send(packet);
    }


}

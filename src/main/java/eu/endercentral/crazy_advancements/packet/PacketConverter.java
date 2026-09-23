package eu.endercentral.crazy_advancements.packet;

import eu.endercentral.crazy_advancements.CrazyAdvancementsAPI;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay;
import eu.endercentral.crazy_advancements.advancement.AdvancementFlag;
import eu.endercentral.crazy_advancements.advancement.ToastNotification;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class PacketConverter {

    private static final AdvancementRewards advancementRewards = new AdvancementRewards(0, HolderSet.direct(new ArrayList<>()), new ArrayList<>(), Optional.empty());

    private static HashMap<NamespacedKey, Float> smallestX = new HashMap<>();
    private static HashMap<NamespacedKey, Float> smallestY = new HashMap<>();

    public static void setSmallestX(NamespacedKey tab, float smallestX) {
        PacketConverter.smallestX.put(tab, smallestX);
    }

    public static float getSmallestX(NamespacedKey key) {
        return smallestX.containsKey(key) ? smallestX.get(key) : 0;
    }

    public static void setSmallestY(NamespacedKey tab, float smallestY) {
        PacketConverter.smallestY.put(tab, smallestY);
    }

    public static float getSmallestY(NamespacedKey key) {
        return smallestY.containsKey(key) ? smallestY.get(key) : 0;
    }

    public static float generateX(NamespacedKey tab, float displayX) {
        return displayX - getSmallestX(tab);
    }

    public static float generateY(NamespacedKey tab, float displayY) {
        return displayY - getSmallestY(tab);
    }

    /**
     * Creates an NMS Advancement
     *
     * @param advancement The Advancement to use as a base
     * @return The NMS Advancement
     */
    public static net.minecraft.advancements.Advancement toNmsAdvancement(Advancement advancement) {
        AdvancementDisplay display = advancement.getDisplay();

        ItemStackTemplate icon = new ItemStackTemplate(CraftItemStack.asNMSCopy(display.getIcon()).getItem());

        final String backgroundTextureString = display.getBackgroundTexture();
        final Optional<ClientAsset.ResourceTexture> backgroundTexture;
        if (backgroundTextureString != null) {
            // The ResourceTexture record needs exactly the following:
            // id: minecraft:block/acacia_log
            // texturePath: minecraft:textures/block/acacia_log.png
            // Therefore, we extract it from the texturePath, which we are assumed to be given.
            final NamespacedKey backgroundTextureKey = NamespacedKey.fromString(backgroundTextureString);
            final String keyValue = backgroundTextureKey.value();
            final boolean startsWithTextures = keyValue.startsWith("textures/");
            final String rawImagePath = startsWithTextures ? keyValue.substring("textures/".length()) : keyValue;
            final Identifier finalId = Identifier.fromNamespaceAndPath(backgroundTextureKey.namespace(), rawImagePath.substring(0, rawImagePath.length() - ".png".length()));
            backgroundTexture = Optional.of(new ClientAsset.ResourceTexture(finalId, Identifier.parse(backgroundTextureString)));
        } else {
            backgroundTexture = Optional.empty();
        }

        final Component title = PaperAdventure.asVanilla(display.getTitle()), description = PaperAdventure.asVanilla(display.getDescription());
        net.minecraft.advancements.DisplayInfo advDisplay = new net.minecraft.advancements.DisplayInfo(icon, title, description, backgroundTexture, display.getFrame().getNMS(), false, false, advancement.hasFlag(AdvancementFlag.SEND_WITH_HIDDEN_BOOLEAN));

        Optional<Identifier> parent = advancement.getParent() == null ? Optional.empty() : Optional.of(CrazyAdvancementsAPI.namespacedKeyToIdentifier(advancement.getParent().getName()));
        net.minecraft.advancements.Advancement adv = new net.minecraft.advancements.Advancement(parent, Optional.of(advDisplay), advancementRewards, advancement.getCriteria().getCriteria(), advancement.getCriteria().getAdvancementRequirements(), false);

        return adv;
    }

    /**
     * Creates an NMS Toast Advancement
     *
     * @param notification The Toast Notification to use as a base
     * @return The NMS Advancement
     */
    public static net.minecraft.advancements.Advancement toNmsToastAdvancement(ToastNotification notification) {
        ItemStackTemplate icon = new ItemStackTemplate(CraftItemStack.asNMSCopy(notification.getIcon()).getItem());

        final Component title = PaperAdventure.asVanilla(notification.getMessage());
        final Component description = PaperAdventure.asVanilla(net.kyori.adventure.text.Component.text("Toast Notification"));
        net.minecraft.advancements.DisplayInfo advDisplay = new net.minecraft.advancements.DisplayInfo(icon, title, description, Optional.empty(), notification.getFrame().getNMS(), true, false, true);

        net.minecraft.advancements.Advancement adv = new net.minecraft.advancements.Advancement(Optional.empty(), Optional.of(advDisplay), advancementRewards, ToastNotification.NOTIFICATION_CRITERIA.getCriteria(), ToastNotification.NOTIFICATION_CRITERIA.getAdvancementRequirements(), false);

        return adv;
    }

    /**
     * Creates a Dummy Advancement<br>Internally used to generate temporary parent advancements that need to be referenced in the packet
     *
     * @param name The name of the Advancement
     * @return the Dummy Advancement
     * @deprecated No longer required for parent dummies. Might be removed in a future version.
     */
    @Deprecated(forRemoval = true, since = "2.1.15")
    public static net.minecraft.advancements.Advancement createDummy(NamespacedKey name) {
        net.minecraft.advancements.Advancement adv = new net.minecraft.advancements.Advancement(Optional.empty(), Optional.empty(), null, new HashMap<>(), new AdvancementRequirements(new ArrayList<>()), false);
        return adv;
    }

}

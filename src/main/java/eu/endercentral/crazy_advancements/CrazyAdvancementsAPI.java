package eu.endercentral.crazy_advancements;

import com.google.gson.*;
import eu.endercentral.crazy_advancements.advancement.Advancement;
import eu.endercentral.crazy_advancements.advancement.serialized.SerializedAdvancement;
import eu.endercentral.crazy_advancements.item.CustomItem;
import eu.endercentral.crazy_advancements.item.SerializedCustomItem;
import eu.endercentral.crazy_advancements.manager.AdvancementManager;
import eu.endercentral.crazy_advancements.packet.AdvancementsPacket;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.resources.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Represents the API's Plugin
 *
 * @author Axel
 *
 */
public class CrazyAdvancementsAPI extends JavaPlugin implements Listener {

    public static final String API_NAMESPACE = "crazy_advancements";

    private static final Gson gson;
    private static final List<String> SELECTORS = Arrays.asList("@a", "@p", "@s", "@r");
    private static CrazyAdvancementsAPI instance;

    static {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    /**
     * Criterion Instance for Internal Use
     */
    public static final Criterion<?> CRITERION = new Criterion<>(new ImpossibleTrigger(), new ImpossibleTrigger.TriggerInstance());


    private static AdvancementPacketReceiver packetReceiver;
    private static HashMap<UUID, NamespacedKey> activeTabs = new HashMap<>();

    private static final List<CustomItem> customItems = new ArrayList<>();
    private AdvancementManager fileAdvancementManager;

    public AdvancementManager getFileAdvancementManager() {
        return this.fileAdvancementManager;
    }

    /**
     * Reloads the API<br>
     * Currently reloads JSON Advancements and Custom Item Definitions
     */
    public void reload() {
        loadCustomItems();
        reloadFileAdvancements();
    }

    private void reloadFileAdvancements() {
        if (fileAdvancementManager != null) {
            for (Player player : new ArrayList<>(fileAdvancementManager.getPlayers())) {
                fileAdvancementManager.removePlayer(player);
            }
            fileAdvancementManager.resetAccessible();
        }
        fileAdvancementManager = new AdvancementManager(new NamespacedKey(API_NAMESPACE, "file"));
        fileAdvancementManager.makeAccessible();
        loadFileAdvancements();

        for (Player player : Bukkit.getOnlinePlayers()) {
            packetReceiver.initPlayer(player);
            fileAdvancementManager.loadProgress(player);
            fileAdvancementManager.addPlayer(player);
        }
    }

    @Override
    public void onLoad() {
        instance = this;
        loadCustomItems();
        fileAdvancementManager = new AdvancementManager(new NamespacedKey(API_NAMESPACE, "file"));
        fileAdvancementManager.makeAccessible();
        loadFileAdvancements();
    }

    private void loadCustomItems() {
        File location = new File(getDataFolder().getAbsolutePath() + File.separator + "custom_items" + File.separator);

        customItems.clear();

        location.mkdirs();
        File[] files = location.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String namespace = file.getName();
                customItems.addAll(loadCustomItemsFromNamespace(namespace, "", file));
            }
        }

        getLogger().info("Loaded " + customItems.size() + " Custom Items");
    }

    private List<CustomItem> loadCustomItemsFromNamespace(String namespace, String path, File location) {
        File[] files = location.listFiles();

        List<CustomItem> items = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                items.addAll(loadCustomItemsFromNamespace(namespace, path + file.getName() + "/", file));
            } else if (file.isFile() && file.getName().endsWith(".json")) {
                FileReader os = null;
                try {
                    os = new FileReader(file);

                    JsonElement element = JsonParser.parseReader(os);
                    os.close();

                    SerializedCustomItem item = gson.fromJson(element, SerializedCustomItem.class);

                    String fileName = file.getName();
                    String key = fileName.substring(0, fileName.length() - 5);//Remove .json
                    items.add(item.deserialize(new NamespacedKey(namespace, path + key)));
                } catch (Exception e) {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    getLogger().warning("Unable to load Custom Item from File " + namespace + "/" + file.getName() + ": " + e.getLocalizedMessage());
                }
            }
        }
        return items;
    }

    private void loadFileAdvancements() {
        File location = new File(getDataFolder().getAbsolutePath() + File.separator + "advancements" + File.separator);

        HashMap<NamespacedKey, SerializedAdvancement> advancements = new HashMap<>();

        location.mkdirs();
        File[] files = location.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String namespace = file.getName();
                advancements.putAll(loadAdvancementsFromNamespace(namespace, "", file));
            }
        }

        List<NamespacedKey> missingAdvancements = new ArrayList<>(advancements.keySet());
        HashMap<NamespacedKey, Advancement> createdAdvancements = new HashMap<NamespacedKey, Advancement>();

        while (missingAdvancements.size() > 0) {
            Iterator<NamespacedKey> missingIterator = missingAdvancements.iterator();
            int processedAdvancements = 0;

            while (missingIterator.hasNext()) {
                NamespacedKey name = missingIterator.next();
                SerializedAdvancement serializedAdvancement = advancements.get(name);
                String parent = serializedAdvancement.getParent();

                if (parent == null || createdAdvancements.containsKey(NamespacedKey.fromString(parent))) {
                    final Advancement advancement = Advancement.fromSerialized(name, serializedAdvancement, createdAdvancements);

                    //Register
                    fileAdvancementManager.addAdvancement(advancement);
                    missingIterator.remove();
                    createdAdvancements.put(name, advancement);
                    processedAdvancements++;
                }
            }

            //Abort adding Advancements if no advancements were able to be processed
            if (processedAdvancements == 0) {
                for (NamespacedKey name : missingAdvancements) {
                    getLogger().warning("Unable to load Advancement " + name + ": Parent does not exist");
                }
                break;
            }
        }
        getLogger().info("Loaded " + createdAdvancements.size() + " advancements from files.");
    }

    private HashMap<NamespacedKey, SerializedAdvancement> loadAdvancementsFromNamespace(String namespace, String path, File location) {
        File[] files = location.listFiles();

        HashMap<NamespacedKey, SerializedAdvancement> advancements = new HashMap<>();

        for (File file : files) {
            if (file.isDirectory()) {
                advancements.putAll(loadAdvancementsFromNamespace(namespace, path + file.getName() + "/", file));
            } else if (file.isFile() && file.getName().endsWith(".json")) {
                FileReader os = null;
                try {
                    os = new FileReader(file);

                    JsonElement element = JsonParser.parseReader(os);
                    os.close();

                    SerializedAdvancement advancement = gson.fromJson(element, SerializedAdvancement.class);

                    String fileName = file.getName();
                    String key = fileName.substring(0, fileName.length() - 5);//Remove .json
                    advancements.put(new NamespacedKey(namespace, path + key), advancement);
                } catch (Exception e) {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    getLogger().warning("Unable to load Advancement from File " + namespace + "/" + file.getName() + ": " + e.getLocalizedMessage());
                }
            }
        }
        return advancements;
    }

    @Override
    public void onEnable() {
        // Init Packet Receiver
        packetReceiver = new AdvancementPacketReceiver();

        for (Player player : Bukkit.getOnlinePlayers()) {
            packetReceiver.initPlayer(player);
            fileAdvancementManager.loadProgress(player);
            fileAdvancementManager.addPlayer(player);
        }

        // Register Events
        Bukkit.getPluginManager().registerEvents(this, this);
        // Commands
        new CommandHandler(this);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AdvancementsPacket packet = new AdvancementsPacket(player, true, null, null);
            packet.send();
        }
    }

    /**
     * Gets the Instance
     *
     * @return The Instance
     */
    public static CrazyAdvancementsAPI getInstance() {
        return instance;
    }

    /**
     * Gets the Gson Instance
     *
     * @return The Gson Instance
     */
    public static Gson getGson() {
        return gson;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        packetReceiver.initPlayer(player);

        //Add Player to File Advancement Manager
        fileAdvancementManager.loadProgress(player);
        // TODO: fix this race condition
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {

            @Override
            public void run() {
                fileAdvancementManager.addPlayer(player);
            }
        }, 2);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        packetReceiver.close(e.getPlayer(), packetReceiver.getHandlers().get(e.getPlayer().getName()));

        //Unload Progress in the File Advancement Manager
        fileAdvancementManager.unloadProgress(e.getPlayer().getUniqueId());
        fileAdvancementManager.unloadVisibilityStatus(e.getPlayer().getUniqueId());
    }

    /**
     * Clears the active tab
     *
     * @param player The player whose Tab should be cleared
     */
    public static void clearActiveTab(Player player) {
        setActiveTab(player, null, true);
    }

    /**
     * Sets the active tab
     *
     * @param player          The player whose Tab should be changed
     * @param rootAdvancement The name of the tab to change to
     */
    public static void setActiveTab(Player player, String rootAdvancement) {
        setActiveTab(player, NamespacedKey.fromString(rootAdvancement));
    }

    /**
     * Sets the active tab
     *
     * @param player          The player whose Tab should be changed
     * @param rootAdvancement The name of the tab to change to
     */
    public static void setActiveTab(Player player, @Nullable NamespacedKey rootAdvancement) {
        setActiveTab(player, rootAdvancement, true);
    }

    static void setActiveTab(Player player, NamespacedKey rootAdvancement, boolean update) {
        if (update) {
            final Identifier tabIdentifier = rootAdvancement == null ? null : namespacedKeyToIdentifier(rootAdvancement);
            ClientboundSelectAdvancementsTabPacket packet = new ClientboundSelectAdvancementsTabPacket(tabIdentifier);
            ((CraftPlayer) player).getHandle().connection.send(packet);
        }
        activeTabs.put(player.getUniqueId(), rootAdvancement);
    }

    /**
     * Gets the active tab
     *
     * @param player Player to check
     * @return The active Tab
     */
    public static NamespacedKey getActiveTab(Player player) {
        return activeTabs.get(player.getUniqueId());
    }

    private static Material getMaterial(String input) {
        for (Material mat : Material.values()) {
            if (mat.name().equalsIgnoreCase(input)) {
                return mat;
            }
        }
        return Material.matchMaterial(input);
    }

    private static CustomItem getCustomItem(String input) {
        NamespacedKey inputName = NamespacedKey.fromString(input);
        for (CustomItem item : customItems) {
            if (item.getName().equals(inputName)) {
                return item;
            }
        }
        return null;
    }

    public static Identifier namespacedKeyToIdentifier(final NamespacedKey key) {
        return Identifier.fromNamespaceAndPath(
            key.namespace(),
            key.value()
        );
    }

    public static ItemStack getItemStack(String input, CommandSender... commandSender) {
        int colonIndex = input.indexOf(':');
        String materialName = colonIndex == -1 ? input : input.substring(0, colonIndex);
        String data = colonIndex == -1 ? "" : input.substring(colonIndex + 1);
        Material material = getMaterial(materialName);

        ItemStack stack;

        if (material == null || !material.isItem()) {
            CustomItem customItem = getCustomItem(input);
            if (customItem == null) {
                return null;
            } else {
                material = customItem.getType();
                stack = new ItemStack(material);
                ItemMeta meta = stack.getItemMeta();
                meta.setCustomModelData(customItem.getCustomModelData());
                stack.setItemMeta(meta);
            }
        } else {
            stack = new ItemStack(material);
        }

        switch (material) {
            case PLAYER_HEAD:
                if (!data.isEmpty()) {
                    if (commandSender.length > 0) {
                        for (String selector : SELECTORS) {
                            if (data.startsWith(selector)) {
                                List<Player> players = new ArrayList<>();
                                for (Entity entity : Bukkit.selectEntities(commandSender[0], data)) {
                                    if (entity instanceof Player) {
                                        players.add((Player) entity);
                                    }
                                }

                                if (players.size() > 0) {
                                    Player player = players.get(0);
                                    SkullMeta meta = (SkullMeta) stack.getItemMeta();
                                    meta.setOwningPlayer(player);
                                    stack.setItemMeta(meta);
                                }
                                return stack;
                            }
                        }
                    }
                    OfflinePlayer player = Bukkit.getOfflinePlayer(data);
                    SkullMeta meta = (SkullMeta) stack.getItemMeta();
                    meta.setOwningPlayer(player);
                    stack.setItemMeta(meta);
                }
            default:
                if (!data.isEmpty() && material.getMaxDurability() > 0) {
                    try {
                        short damage = Short.parseShort(data);
                        Damageable meta = (Damageable) stack.getItemMeta();
                        meta.setDamage(damage);
                        stack.setItemMeta(meta);
                    } catch (ClassCastException | NumberFormatException e) {
                    }
                }
                return stack;
        }
    }

}

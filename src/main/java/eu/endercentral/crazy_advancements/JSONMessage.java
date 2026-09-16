package eu.endercentral.crazy_advancements;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a Message in JSON Format
 *
 * @author Axel
 *
 */
public class JSONMessage {

    private static final Provider COMPONENT_SERIALIZER_PROVIDER = new TextHolderLookupProvider();


    private final BaseComponent json;

    /**
     * Constructor for creating a JSON Message
     *
     * @param json A JSON representation of an ingame Message <a href="https://www.spigotmc.org/wiki/the-chat-component-api/">Read More</a>
     */
    public JSONMessage(BaseComponent json) {
        this.json = json;
    }

    /**
     * Gets the Message as a BaseComponent
     *
     * @return the BaseComponent of an ingame Message
     */
    public BaseComponent getJson() {
        return json;
    }

    /**
     * Gets an NMS representation of an ingame Message
     *
     * @return An {@link Component} representation of an ingame Message
     */
    public Component getBaseComponent() {
        return ComponentSerialization.CODEC.parse(
                COMPONENT_SERIALIZER_PROVIDER.createSerializationContext(JsonOps.INSTANCE),
                JsonParser.parseString(ComponentSerializer.toString(json))
        ).getOrThrow();
    }

    @Override
    public String toString() {
        return json.toPlainText();
    }


    private static class TextHolderLookupProvider implements Provider {

        @Override
        public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
            return Stream.empty();
        }

        @Override
        public <T> Optional<RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryRef) {
            return Optional.empty();
        }

    }

}

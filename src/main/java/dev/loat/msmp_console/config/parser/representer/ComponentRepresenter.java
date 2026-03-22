package dev.loat.msmp_console.config.parser.representer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;


/**
 * Custom SnakeYAML representer for serializing Minecraft chat components to YAML.
 */
public class ComponentRepresenter extends Representer {
    /**
     * Constructs a new component representer for serializing components (components -> YAML).
     * 
     * @param configClass The configuration class to associate with this representer.
     * @param options The DumperOptions to use.
     */
    public ComponentRepresenter(
        Class<?> configClass,
        DumperOptions options
    ) {
        super(options);

        this.addClassTag(configClass, Tag.MAP);

        this.representers.put(Component.class, (data) -> this.representComponent((Component) data));
        this.representers.put(MutableComponent.class, (data) -> this.representComponent((Component) data));
    }

    /**
     * Represents a Component as a Node.
     * 
     * @param component The Component to represent.
     * @return The Node representation of the Component.
     */
    private Node representComponent(Component component) {
        JsonElement json = ComponentSerialization.CODEC
            .encodeStart(JsonOps.INSTANCE, component)
            .getOrThrow(err -> new IllegalStateException("Failed to encode Component: " + err));

        return this.represent(new Gson().fromJson(json, Object.class));
    }
}

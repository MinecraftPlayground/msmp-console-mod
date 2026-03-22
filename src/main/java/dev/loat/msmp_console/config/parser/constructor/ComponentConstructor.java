package dev.loat.msmp_console.config.parser.constructor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;


/**
 * Custom SnakeYAML constructor for parsing Minecraft chat components from YAML.
 * 
 * This constructor extends the default SnakeYAML constructor and adds support for parsing YAML nodes into Minecraft's Component objects.
 * It converts YAML nodes into JSON elements and then uses Minecraft's ComponentSerialization to decode them into Component instances.
 */
public class ComponentConstructor extends Constructor {

    /**
     * Constructs a new component constructor for parsing components (YAML -> components).
     */
    public ComponentConstructor(
        Class<?> configClass,
        LoaderOptions options
    ) {
        super(configClass, options);
    }

    @Override
    protected Object constructObject(Node node) {
        if (Component.class.isAssignableFrom(node.getType())) {
            return this.constructComponent(node);
        }
        return super.constructObject(node);
    }

    /**
     * Constructs a component from a YAML node.
     * 
     * @param node The YAML node to construct the component from.
     * @return The constructed component.
     */
    private Component constructComponent(Node node) {
        JsonElement json = nodeToJson(node);
        
        return ComponentSerialization.CODEC
            .decode(JsonOps.INSTANCE, json)
            .getOrThrow(error -> new IllegalStateException("Failed to decode Component:\n%s".formatted(error)))
            .getFirst();
    }

    /**
     * Converts a YAML node into a JSON element.
     * 
     * This method supports scalar nodes (with tags bool, int, float, and null), sequence nodes, and mapping nodes.
     * 
     * @param node The YAML node to convert.
     * @return The converted JSON element.
     * @throws IllegalArgumentException If the node type is not supported.
     */
    private JsonElement nodeToJson(Node node) {
        if (node instanceof ScalarNode scalar) {
            Object value = constructScalar(scalar);
            if (value == null) {
                return JsonNull.INSTANCE;
            } else if (node.getTag().equals(Tag.BOOL)) {
                return new JsonPrimitive(Boolean.parseBoolean(value.toString()));
            } else if (node.getTag().equals(Tag.INT)) {
                return new JsonPrimitive(Integer.parseInt(value.toString()));
            } else if (node.getTag().equals(Tag.FLOAT)) {
                return new JsonPrimitive(Double.parseDouble(value.toString()));
            } else {
                return new JsonPrimitive(String.valueOf(value).translateEscapes());
            }
        } else if (node instanceof SequenceNode sequence) {
            JsonArray array = new JsonArray();
            for (Node child : sequence.getValue()) {
                array.add(nodeToJson(child));
            }
            return array;
        } else if (node instanceof MappingNode mapping) {
            JsonObject object = new JsonObject();
            for (NodeTuple tuple : mapping.getValue()) {
                ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
                String key = constructScalar(keyNode);
                JsonElement value = nodeToJson(tuple.getValueNode());
                object.add(key, value);
            }
            return object;
        }
        throw new IllegalArgumentException("Unexpected node type: " + node.getClass().getName());
    }
}

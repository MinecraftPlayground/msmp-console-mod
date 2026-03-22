package dev.loat.msmp_console.config.parser;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import dev.loat.msmp_console.config.annotation.Comment;
import dev.loat.msmp_console.config.parser.constructor.ComponentConstructor;
import dev.loat.msmp_console.config.parser.representer.ComponentRepresenter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class YamlConfig<ConfigClass> {
    private final String filePath;
    private final Class<ConfigClass> configClass;

    public YamlConfig(
        String filePath,
        Class<ConfigClass> configClass
    ) {
        this.filePath = filePath;
        this.configClass = configClass;
    }

    /**
     * Serializes a configuration object to a YAML file with comments.
     *
     * @param config The configuration object to serialize.
     * @throws IOException If an error occurs while writing the file.
     */
    public void serialize(ConfigClass config) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        Representer componentRepresenter = new ComponentRepresenter(this.configClass, options);

        Yaml yaml = new Yaml(componentRepresenter, options);

        String yamlString = yaml.dump(config);
        String[] lines = yamlString.split("\n");

        Field[] fields = this.configClass.getDeclaredFields();
        Map<String, List<String>> commentsMap = new HashMap<>();
        for (Field field : fields) {
            @SuppressWarnings("null")
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                String[] commentLines = comment.value().split("\n");
                List<String> formattedComments = new ArrayList<>();
                for (String commentLine : commentLines) {
                    String trimmed = commentLine.trim();
                    if (!trimmed.isEmpty()) {
                        formattedComments.add("# " + trimmed);
                    } else {
                        formattedComments.add("#");
                    }
                }
                commentsMap.put(field.getName(), formattedComments);
            }
        }

        List<String> modifiedLines = new ArrayList<>();
        for (String line : lines) {
            for (String fieldName : commentsMap.keySet()) {
                if (line.startsWith(fieldName + ":")) {
                    modifiedLines.addAll(commentsMap.get(fieldName));
                    break;
                }
            }
            modifiedLines.add(line);
        }

        Files.write(path, modifiedLines);
    }

    /**
     * Parses a YAML file and returns a configuration object.
     *
     * @return The configuration object reflecting the YAML file structure.
     * @throws IOException If an error occurs while reading the file or casting.
     */
    public ConfigClass parse() throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
            throw new IllegalStateException("YAML file was newly created and is empty: " + filePath);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            LoaderOptions options = new LoaderOptions();
            options.setAllowDuplicateKeys(false);

            Constructor componentConstructor = new ComponentConstructor(this.configClass, options);

            Yaml yaml = new Yaml(componentConstructor);

            Object result = yaml.load(inputStream);

            if (result == null) {
                throw new IllegalStateException("No data found in YAML file: " + filePath);
            }

            return this.configClass.cast(result);
        }
    }
}

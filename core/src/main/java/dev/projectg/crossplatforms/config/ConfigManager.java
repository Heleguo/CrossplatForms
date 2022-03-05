package dev.projectg.crossplatforms.config;

import dev.projectg.crossplatforms.Logger;
import dev.projectg.crossplatforms.action.ActionSerializer;
import dev.projectg.crossplatforms.command.DispatchableCommand;
import dev.projectg.crossplatforms.command.DispatchableCommandSerializer;
import dev.projectg.crossplatforms.interfacing.bedrock.BedrockForm;
import dev.projectg.crossplatforms.interfacing.bedrock.BedrockFormSerializer;
import dev.projectg.crossplatforms.interfacing.bedrock.custom.CustomComponent;
import dev.projectg.crossplatforms.interfacing.bedrock.custom.ComponentSerializer;
import dev.projectg.crossplatforms.interfacing.bedrock.FormImageSerializer;
import dev.projectg.crossplatforms.utils.FileUtils;
import io.leangen.geantyref.TypeToken;
import lombok.Getter;
import org.geysermc.cumulus.util.FormImage;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ConfigManager {

    private final File directory;
    private final Logger logger;
    private final YamlConfigurationLoader.Builder loaderBuilder;
    private final Set<ConfigId> identifiers = new HashSet<>();
    private final Map<Class<? extends Configuration>, Configuration> configurations = new HashMap<>();

    @Getter
    private final ActionSerializer actionSerializer = new ActionSerializer();

    public ConfigManager(Path directory, Logger logger) {
        this.directory = directory.toFile();
        this.logger = logger;
        // type serializers for abstract classes and external library classes
        loaderBuilder = YamlConfigurationLoader.builder();
        loaderBuilder.defaultOptions(opts -> (opts.serializers(builder -> {
            builder.registerExact(BedrockForm.class, new BedrockFormSerializer());
            builder.registerExact(FormImage.class, new FormImageSerializer());
            builder.registerExact(CustomComponent.class, new ComponentSerializer());
            builder.registerExact(DispatchableCommand.class, new DispatchableCommandSerializer());
            builder.register(new TypeToken<>() {}, actionSerializer); // List<Action>
        })));
        // don't initialize default values for object values
        // default parameters provided to ConfigurationNode getter methods should not be set to the node
        loaderBuilder.defaultOptions(opts -> opts.implicitInitialization(false).shouldCopyDefaults(false));
        loaderBuilder.nodeStyle(NodeStyle.BLOCK); // don't inline lists, maps, etc
        loaderBuilder.indent(2);
    }

    public void register(ConfigId id) {
        identifiers.add(id);
    }

    public void serializers(Consumer<TypeSerializerCollection.Builder> builder) {
        loaderBuilder.defaultOptions(opts -> (opts.serializers(builder)));
    }

    @SuppressWarnings("unchecked")
    public <T extends Configuration> Optional<T> getConfig(Class<T> clazz) {
        return Optional.ofNullable((T) configurations.get(clazz));
    }

    /**
     * Load every config in {@link ConfigId}
     * @return false if there was a failure loading any of configurations
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean load() {
        for (ConfigId configId : identifiers) {
            try {
                if (!loadConfig(configId)) {
                    return false;
                }
            } catch (IOException e) {
                logger.severe("Failed to load configuration " + configId.fileName);
                String message = e.getMessage();
                if (logger.isDebug() || message.contains("Unknown error")) {
                    // message is useless on its own if unknown
                    e.printStackTrace();
                } else {
                    logger.severe("Enabled debug mode for further information.");
                    logger.severe(message);
                }
                if (!useMinimalDefaults(configId)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Load a configuration from file. The config will only be loaded into memory if there were zero errors.
     * @param config The configuration to load
     * @return The success state
     */
    private boolean loadConfig(ConfigId config) throws IOException {
        File file = FileUtils.fileOrCopiedFromResource(new File(directory, config.fileName));
        YamlConfigurationLoader loader = loaderBuilder.file(file).build();
        ConfigurationNode nodes = loader.load();

        boolean correctVersion = true;
        if (nodes.hasChild(Configuration.VERSION_KEY)) { // ensure version is correct
            int currentVersion = nodes.node(Configuration.VERSION_KEY).getInt();
            if (currentVersion != config.version) {
                if (config.updater == null) {
                    logger.severe(config.fileName + " must have a version of " + config.version + " but is at " + currentVersion + ". Please back it up and regenerate a new config.");
                    correctVersion = false;
                } else if (currentVersion < config.minimumVersion || currentVersion > config.version) {
                    logger.severe(config.fileName + " must have a version between " + config.minimumVersion + " and " + config.version + " but is at " + currentVersion + ". Please back it up and regenerate a new config.");
                    correctVersion = false;
                } else {
                    ConfigurationNode copy = nodes.copy(); // keep an old copy to save to file if an update happens
                    ConfigurationTransformation.Versioned updater = config.updater.get(); // transformer for performing updates
                    int startVersion = updater.version(nodes);
                    updater.apply(nodes); // update if necessary
                    int endVersion = updater.version(nodes);
                    if (startVersion != endVersion) {
                        loaderBuilder.file(new File(directory, "old_" + config.fileName)).build().save(copy); // save the old copy
                        loader.save(nodes); // save the updated version
                    }
                    if (endVersion == config.version) {
                        logger.info("Updated " + config.fileName + " from version " + startVersion + " to " + endVersion);
                    } else {
                        logger.severe("Failed to update " + config.fileName + " from version " + startVersion + " to " + endVersion);
                        correctVersion = false;
                    }
                }
            }
        } else {
            logger.severe(config.fileName + " must defined a " + Configuration.VERSION_KEY + ". Please back it up and regenerate a new config.");
            correctVersion = false;
        }

        Configuration mapped;
        if (correctVersion) {
            mapped = nodes.get(config.clazz); // Map it to the object
            if (mapped == null) {
                logger.severe("Failed to deserialize " + config.fileName + " to " + config.clazz + ": Mapped object returned null.");
            }
        } else {
            mapped = null;
        }

        if (mapped == null) {
            return useMinimalDefaults(config);
        } else {
            configurations.put(config.clazz, mapped);
            return true;
        }
    }


    private boolean useMinimalDefaults(ConfigId config) {
        try {
            // Get the default values so that the plugin can be reloaded at a later time
            configurations.put(config.clazz, config.clazz.getConstructor().newInstance());
            logger.warn("Falling back to MINIMAL DEFAULTS for configuration: " + config.fileName);
            return true;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            logger.severe("Failed to fallback to defaults for configuration " + config.fileName + ": " + e.getLocalizedMessage());
            if (logger.isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
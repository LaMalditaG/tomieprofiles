package tomieprofiles;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import de.exlll.configlib.YamlConfigurations;
import tomieprofiles.config.TomieConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final Path configDirectoryPath;
    private TomieConfig config;
    private final Logger logger;

    public ConfigManager(Path configDirectoryPath, Logger logger) {
        this.configDirectoryPath = configDirectoryPath;
        this.config = new TomieConfig();
        this.logger = logger;
    }

    private Path getConfigFilePath() {
        return configDirectoryPath.resolve("config.yaml");
    }

    public Path getConfigDirectoryPath() {
        return configDirectoryPath;
    }

    public TomieConfig getConfig() {
        return config;
    }

    public void initConfigIfNotExists() throws IOException {
        if (!Files.exists(configDirectoryPath)) {
            logger.info("Config directory does not exist, creating");
            configDirectoryPath.toFile().mkdirs();
        }

        if (!Files.exists(getConfigFilePath())) {
            logger.info("Config file does not exist, creating");
            YamlConfigurations.save(getConfigFilePath(),TomieConfig.class,config);
        }
    }

    public void loadConfig() throws IOException {
        logger.info("Loading config");
        config = YamlConfigurations.load(getConfigFilePath(), TomieConfig.class);
    }

}
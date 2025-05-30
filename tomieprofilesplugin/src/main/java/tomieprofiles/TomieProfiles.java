package tomieprofiles;

import tomieprofiles.command.SwapProfileCommand;
import tomieprofiles.config.TomieConfig;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;

import de.exlll.configlib.YamlConfigurations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;

@Plugin(id = "tomieprofiles", name = "TomieProfiles", version = "0.1.0-SNAPSHOT",
        url = "https://example.org", description = "I did it!", authors = {"LaMalditaG"})
public class TomieProfiles {
    public enum CommandResult{
        OK, OUT_OF_RANGE,IN_USE
    }

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Path profileDataPath;
    private final ConfigManager configManager;

    BaseProfileList profiles = new BaseProfileList();

    @Inject
    public TomieProfiles(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        
        profileDataPath = this.dataDirectory.resolve("profileData.yaml");
        this.configManager = new ConfigManager(dataDirectory, logger);

        logger.info("TomieProfiles created");
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event){
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        loadData();

        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("tomieswap")
            .aliases("profileswap")
            .plugin(this)
            .build();

        SimpleCommand commandToRegister = new SwapProfileCommand(this);

        commandManager.register(commandMeta, commandToRegister);

        server.getEventManager().register(this, new LoginListener(this, logger));

        // Config
        loadConfig();
    }


    public GameProfile asignProfile(GameProfile baseProfile){
        GameProfile out;
        if(profiles.containsBase(baseProfile.getId())){
            logger.info("BaseProfile already exists");
            out = profiles.get(baseProfile.getId()).createChildGameProfile(baseProfile);
        }else{
            logger.info("BaseProfile doesnt exist");
            profiles.put(baseProfile.getId(), new MiniProfileGroup(baseProfile.getId(), baseProfile.getName(), logger));
            out = baseProfile;
        }
        saveData();
        return out;
    }

    public UUID getUuidFromBase(UUID uuid,int id){
        if(id == 0) return uuid;        
        return profiles.get(uuid).getUuid(id);
    }

    public CommandResult setNextConnectionMiniProfile(UUID uuid, int id){
        MiniProfileGroup profileGroup = profiles.getProfileGroupFromChild(uuid);
        UUID newUuid = profileGroup.getUuid(id);
        
        if(newUuid == null){
            return CommandResult.OUT_OF_RANGE;
        }

        if(isPlayerConnected(newUuid)){
            logger.info("BaseProfile "+id+" is already in use for "+uuid);
            return CommandResult.IN_USE;
        }
        profileGroup.setNextConnectionId(id);
        return CommandResult.OK;
    }

    public boolean isPlayerConnected(UUID uuid){
        logger.info(uuid.toString());
        logger.info(server.getAllPlayers().toString());
        for (var player : server.getAllPlayers()) {
            if(player.getUniqueId().equals(uuid))
                return true;
        }
        return false;
    }

    public boolean isPlayerConnected(String name){
        logger.info(server.getAllPlayers().toString());
        for (var player : server.getAllPlayers()) {
            if(player.getUsername().equals(name))
                return true;
        }
        return false;
    }

    public void setProfileDisconnect(UUID uuid){
        profiles.setProfileDisconnect(uuid);
    }

    public void saveData(){
        logger.info("Saving data");
        if(!Files.isDirectory(dataDirectory)) {
            try{
                Files.createDirectory(dataDirectory);
            }catch(IOException i){
                i.printStackTrace();
                return;
            }
        }

        YamlConfigurations.save(profileDataPath, BaseProfileList.class, profiles);
    }

    public void loadData(){
        if(!Files.exists(profileDataPath)) return;
        profiles = YamlConfigurations.load(profileDataPath, BaseProfileList.class);
        profiles.updateLogger(logger);
    }

    public TomieConfig.Server getServerConfig(String servername){
        var serversConfig = configManager.getConfig().getServers();

        TomieConfig.Server out = null;
        if(serversConfig == null) return out;
        for(var serverConfig : serversConfig){
            if(serverConfig.getServerName().equals(servername)){
                out = serverConfig;
            }
        }
        return out;
    }

    private void loadConfig(){
        try{
            configManager.initConfigIfNotExists();
        }catch(Exception e){
            logger.error("Error creating config", e);
            logger.error("Server will shutdown");
            this.server.shutdown();
        }

        try{
            configManager.loadConfig();
        }catch(Exception e){
            logger.error("Error loading config", e);
        }
    }
}
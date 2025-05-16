package tomieprofiles;

import tomieprofiles.command.SwapProfileCommand;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.proxy.ServerConnection;

import net.elytrium.limboapi.api.player.LimboPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;

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

    BaseProfileList profiles = new BaseProfileList();

    @Inject
    public TomieProfiles(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        profileDataPath = this.dataDirectory.resolve("profileData.bin");

        logger.info("TomieProfiles created");

        loadData();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event){
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("tomieswap")
            .aliases("profileswap")
            .plugin(this)
            .build();

        SimpleCommand commandToRegister = new SwapProfileCommand(this);

        commandManager.register(commandMeta, commandToRegister);
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event){
        server.getEventManager().register(this, new LoginListener(this, logger));
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
        profileGroup.setNextId(id);
        return CommandResult.OK;
    }

    // public void swapProfiles(UUID uuid0, UUID uuid1){
    //     logger.info("Swapping");
    //     for(var player : server.getAllPlayers()){
    //         if(player.getUniqueId().equals(uuid0)){
    //             if(player.getCurrentServer().isPresent()){
    //                 RegisteredServer gameServer = player.getCurrentServer().get().getServer();
    //                 final TextComponent textComponent = Component.text().content("Swapping").build();
    //                 for(var property : player.getGameProfile().getProperties()){
    //                     logger.info(property.getName()+ " : " +property.getValue());
                        
    //                 }
    //                 player.getGameProfile().getName();
                    
                    
    //             }
    //         }
    //     }
    // }

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
        if(!Files.isDirectory(dataDirectory)) {
            try{
                Files.createDirectory(dataDirectory);
            }catch(IOException i){
                i.printStackTrace();
                return;
            }
        }
        try {
            FileOutputStream fileOut = new FileOutputStream(profileDataPath.toString());
            
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(profiles);
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public void loadData(){
        if(!Files.exists(profileDataPath)) return;
        try {
            FileInputStream fileIn = new FileInputStream(profileDataPath.toString());
            ObjectInputStream in = new ObjectInputStream(fileIn);
            profiles = (BaseProfileList) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Profile class not found");
            c.printStackTrace();
        }
    }
}
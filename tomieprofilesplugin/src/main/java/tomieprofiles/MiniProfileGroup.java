package tomieprofiles;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;

import com.google.gson.JsonObject;
import com.velocitypowered.api.util.GameProfile;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class MiniProfileGroup implements Serializable{

    static class MiniProfile implements Serializable{
        UUID uuid;
        transient boolean isConnected = false;
        MiniProfile(UUID uuid,boolean connected){
            this.uuid = uuid;
            isConnected = connected;
        }
        MiniProfile(UUID uuid){
            this.uuid = uuid;
        }
        // static Object serialize(@NotNull final Object o) throws IOException {
        //     final var json = new JsonObject();
        //     MiniProfile miniProfile = (MiniProfile) o;
        //     json.addProperty("uuid", miniProfile.uuid.toString());
        //     return json;
        // }
    
        // static MiniProfile deserialize(@NotNull final Object o) throws IOException {
        //     final var json = (JsonObject) o;
            
        //     UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        //     return new MiniProfile(uuid);
        // }
    }

    UUID baseUuid;
    Map<Integer,MiniProfile> childProfiles;
    String username;
    Logger logger;
    int nextId = -1;

    public MiniProfileGroup(UUID id, String name, Logger logger){
        baseUuid = id;
        username = name;
        this.logger = logger;
        childProfiles = new HashMap<>();
        childProfiles.put(0, new MiniProfile(id,true));
    }

    // static Object serialize(@NotNull final Object o) throws IOException {
    //     final var json = new JsonObject();
    //     MiniProfileGroup miniProfile = (MiniProfileGroup) o;
    //     json.addProperty("uuid", miniProfile.baseUuid.toString());
    //     return json;
    // }

    // static MiniProfileGroup deserialize(@NotNull final Object o) throws IOException {
    //     final var json = (JsonObject) o;
        
    //     UUID uuid = UUID.fromString(json.get("uuid").getAsString());
    //     String name = json.get("name").getAsString();
    //     Map<Integer,MiniProfile> childProfiles = Map.deserialize(json.getAsJsonObject("childProfiles"));
    //     return new MiniProfileGroup(uuid);
    // }

    public String getBaseUsername(){
        return username;
    }
    public String getUsername(int i){
        if(i <= 0) return username;
        return StringUtils.left(i + "_" + username, 16);
    }
    public UUID getUuid(int i){
        if(childProfiles.containsKey(i)){
            return childProfiles.get(i).uuid;
        }
        return null;
    }
    public boolean setNextId(int id){
        if(childProfiles.containsKey(id)){
            nextId = id;
            return true;
        }

        return false;
    }

    private UUID generateUUIDFromName(String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(name.getBytes(StandardCharsets.UTF_8));

            long mostSigBits = 0;
            long leastSigBits = 0;

            for (int i = 0; i < 8; i++) {
                mostSigBits = (mostSigBits << 8) | (hash[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                leastSigBits = (leastSigBits << 8) | (hash[i] & 0xff);
            }

            mostSigBits &= ~0x000000000000F000L;
            mostSigBits |= 0x0000000000003000L;

            leastSigBits &= ~(0xc000000000000000L);
            leastSigBits |= 0x8000000000000000L;

            return new UUID(mostSigBits, leastSigBits);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return UUID.randomUUID();
        }
    }

    public void setProfileDisconnect(UUID uuid){
        for(var profile : childProfiles.values()){
            if(profile.uuid.equals(uuid)){
                profile.isConnected = false;
                logger.info("MiniProfile "+uuid + " disconnected");
            }
        }
    }

    public boolean hasChild(UUID uuid){
        boolean found = false;
        for(var i : childProfiles.values()){
            if(i.uuid.equals(uuid))
                found = true;
        }
        return found;
    }

    public GameProfile createChildGameProfile(GameProfile profile) {
        if(!profile.getName().equals(username) ) return null;
        if(!profile.getId().equals(baseUuid)) return null;

        String newName;
        UUID newUUID;

        int outId = -1;
        if(nextId>=0){
            if(childProfiles.get(nextId).isConnected)
                return null;
            outId = nextId;
            nextId = -1;
        }else{
            boolean found = false;
            for(var id : childProfiles.keySet()){
                if(found==false&&!childProfiles.get(id).isConnected){
                    found = true;
                    outId = id;
                }
            }

            if(found == false){
                outId = childProfiles.size();
            }
        }


        if(outId == 0) return profile;

        newName = getUsername(outId);
        newUUID = generateUUIDFromName(newName + "$");

        if(!childProfiles.containsKey(outId))
            childProfiles.put(outId, new MiniProfile(newUUID,true));
        else
            childProfiles.get(outId).isConnected = true;

        return new GameProfile(newUUID,newName,profile.getProperties());
    }
}

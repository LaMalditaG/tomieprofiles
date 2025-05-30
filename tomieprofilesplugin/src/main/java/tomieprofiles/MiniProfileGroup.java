package tomieprofiles;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;

import com.velocitypowered.api.util.GameProfile;

import de.exlll.configlib.Configuration;

import org.apache.commons.lang3.StringUtils;

@Configuration
public class MiniProfileGroup {

    @Configuration
    static class MiniProfile {
        private UUID uuid;
        transient private boolean isConnected = false;
        public MiniProfile(){}

        MiniProfile(UUID uuid,boolean connected){
            this.uuid = uuid;
            isConnected = connected;
        }
        MiniProfile(UUID uuid){
            this.uuid = uuid;
        }
    }

    public MiniProfileGroup(){}

    private UUID baseUuid;
    private String username;
    private int nextId = -1;
    private Map<Integer,MiniProfile> childProfiles;
    transient private Logger logger;

    public void setBaseUuid(UUID uuid){
        baseUuid = uuid;
    }

    public UUID getBaseUuid(){
        return baseUuid;
    }

    public void setLogger(Logger logger){
        this.logger = logger;
    }

    public MiniProfileGroup(UUID id, String name, Logger logger){
        baseUuid = id;
        username = name;
        this.logger = logger;
        childProfiles = new HashMap<>();
        childProfiles.put(0, new MiniProfile(id,true));
    }

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
    public boolean setNextConnectionId(int id){
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
            return UUID.randomUUID();
        }
    }

    public void setProfileDisconnect(UUID uuid){
        for(var profile : childProfiles.values()){
            if(profile.uuid.equals(uuid)){
                profile.isConnected = false;
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
        logger.info("Creating child profile");
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
                    logger.info("id " + id + " " +childProfiles.get(id).isConnected);
                }
            }

            if(found == false){
                outId = childProfiles.size();
            }
        }

        newName = getUsername(outId);
        newUUID = generateUUIDFromName(newName + "$");

        if(!childProfiles.containsKey(outId))
            childProfiles.put(outId, new MiniProfile(newUUID,true));
        else
            childProfiles.get(outId).isConnected = true;

        if(outId == 0) return profile;

        return new GameProfile(newUUID,newName,profile.getProperties());
    }
}

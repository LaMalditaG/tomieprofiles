package tomieprofiles;

import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;

import de.exlll.configlib.Configuration;

@Configuration
public class BaseProfileList{

    private ArrayList<MiniProfileGroup> profiles;

    public BaseProfileList(){
        profiles = new ArrayList<>();
    }

    public boolean containsBase(UUID uuid){
        boolean out = false;
        for(var profile : profiles){
            if(profile.getBaseUuid().equals(uuid)) out = true;
        }
        return out;
    }

    public MiniProfileGroup get(UUID uuid){
        MiniProfileGroup out = null;
        for(var profile : profiles){
            if(profile.getBaseUuid().equals(uuid)) out = profile;
        }
        return out;
    }

    public UUID getBaseProfile(UUID uuid){
        UUID baseProfileUuid = null;
        for(var profile : profiles){
            if(profile.hasChild(uuid))
                baseProfileUuid = profile.getBaseUuid();
            
        }
        return baseProfileUuid;
    }

    public MiniProfileGroup getProfileGroupFromChild(UUID uuid){
        MiniProfileGroup baseProfileUuid = null;
        for(var profile : profiles){
            if(profile.hasChild(uuid))
                baseProfileUuid = profile;
            
        }
        return baseProfileUuid;
    }

    public void put(UUID uuid, MiniProfileGroup profileGroup){
        // profiles.put(uuid, profileGroup);
        profileGroup.setBaseUuid(uuid);
        profiles.add(profileGroup);
        
    }

    public void setProfileDisconnect(UUID uuid){
        for(var profile : profiles){
            if(profile.hasChild(uuid)){
                profile.setProfileDisconnect(uuid);
            }
        }
    }

    public void updateLogger(Logger logger){
        for(var miniProfileGroup : profiles){
            miniProfileGroup.setLogger(logger);
        }
    }
}

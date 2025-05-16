package tomieprofiles;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class BaseProfileList implements Serializable{

    private final Map<UUID, MiniProfileGroup> profiles;

    public BaseProfileList(){
        profiles = new HashMap<>();
    }

    public static Object serialize(@NotNull final Object o) throws IOException {
        final var portal_block = (BaseProfileList) o;
        final var json = new JsonObject();
        // json.put("block", to_json());
        // json.put("type", to_json());
        return json;
    }

    public static BaseProfileList deserialize(@NotNull final Object o) throws IOException {
        final var json = (JsonObject) o;

        return new BaseProfileList();
    }

    public boolean containsBase(UUID uuid){
        return profiles.containsKey(uuid);
    }

    public MiniProfileGroup get(UUID uuid){
        return profiles.get(uuid);
    }

    public UUID getBaseProfile(UUID uuid){
        UUID baseProfileUuid = null;
        for(var profile : profiles.values()){
            if(profile.hasChild(uuid))
                baseProfileUuid = profile.baseUuid;
            
        }
        return baseProfileUuid;
    }

    public MiniProfileGroup getProfileGroupFromChild(UUID uuid){
        MiniProfileGroup baseProfileUuid = null;
        for(var profile : profiles.values()){
            if(profile.hasChild(uuid))
                baseProfileUuid = profile;
            
        }
        return baseProfileUuid;
    }

    public void put(UUID uuid, MiniProfileGroup profileGroup){
        profiles.put(uuid, profileGroup);
    }

    public void setProfileDisconnect(UUID uuid){
        for(var profile : profiles.values()){
            if(profile.hasChild(uuid)){
                profile.setProfileDisconnect(uuid);
            }
        }
    }
}

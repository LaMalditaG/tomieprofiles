package tomieprofiles.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;

@Configuration
public class TomieServerConfig{
    @Comment("Not all versions allow to overwrite messages, you may need and third party plugin to use this")
    private boolean overwriteMessages;
    @Comment("active doesnt do anything yet")
    private boolean active;
    private String serverName;

    public boolean getOverwriteMessages(){
        return overwriteMessages;
    }

    public String getServerName(){
        return serverName;
    }

    public boolean getActive(){
        return active;
    }

    public TomieServerConfig() {
        
        overwriteMessages = false;
        serverName = "";
        active = true;
    }
}

package tomieprofiles.config;

import java.io.Serializable;
import java.util.ArrayList;

import de.exlll.configlib.Configuration;

@Configuration
public class TomieConfig {
    private ArrayList<TomieConfig.Server> servers;

    public ArrayList<TomieConfig.Server> getServers(){
        return servers;
    }

    public void setServers(ArrayList<TomieConfig.Server> s){
        servers = s;
    }

    public TomieConfig() {
        servers = new ArrayList<>();
    }

    @Configuration
    static public class Server implements Serializable{
        private boolean overwriteMessages;
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

        public Server() {
            overwriteMessages = true;
            serverName = "";
            active = true;
        }
    }
}

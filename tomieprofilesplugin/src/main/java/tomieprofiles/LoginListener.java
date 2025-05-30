package tomieprofiles;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import static com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import tomieprofiles.config.TomieConfig;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class LoginListener {

    Logger logger;
    TomieProfiles controller;

    LoginListener(TomieProfiles controller,Logger logger){
        this.logger = logger;
        this.controller = controller;
    }

    @Subscribe(priority = 0)
    public void onGameProfileRequest(GameProfileRequestEvent event){
        event.setGameProfile(controller.asignProfile(event.getOriginalProfile()));
    }

    @Subscribe(priority = 0)
    public void onPlayerChat(PlayerChatEvent playerChatEvent){
        Player player = playerChatEvent.getPlayer();
        if(playerChatEvent.getPlayer().getCurrentServer().isPresent()){
            RegisteredServer server = player.getCurrentServer().get().getServer();
            TomieConfig.Server config = controller.getServerConfig(server.getServerInfo().getName());
            if(config!=null&&config.getActive()&&config.getOverwriteMessages()){
                logger.info("Rewritting message");
                playerChatEvent.setResult(denied());
                
                String text = playerChatEvent.getMessage();
                
                Component msg = parseMessage( "<<player>> <message>", List.of(
                    new ChatTemplate("player", player.getUsername(), false),
                    new ChatTemplate("server", server.getServerInfo().getName(), false),
                    new ChatTemplate("message", text, true)
                ));
                server.sendMessage(msg);
            }
        }
    }

    @Subscribe(priority =  0)
    public void onDisconnect(DisconnectEvent event){
        controller.setProfileDisconnect(event.getPlayer().getUniqueId());
    }

    private Component parseMessage(String input, List<ChatTemplate> templates) {
        List<TagResolver.Single> list = new ArrayList<>();

        for (ChatTemplate tmpl : templates) {
            if (tmpl.parse)
                list.add(Placeholder.parsed(tmpl.name, tmpl.value));
            else
                list.add(Placeholder.parsed(tmpl.name, Component.text(tmpl.value).content()));
        }

        return MiniMessage.miniMessage().deserialize(input, list.toArray(TagResolver[]::new));
    }

    static final class ChatTemplate {
        final String name;
        final String value;
        final Boolean parse; // should we run through minimessage's parsing?

        public ChatTemplate(String name, String value, Boolean shouldParse) {
            this.name = name;
            this.value = value;
            this.parse = shouldParse;
        }

    }
}

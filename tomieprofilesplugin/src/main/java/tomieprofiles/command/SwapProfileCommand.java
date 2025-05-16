package tomieprofiles.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.concurrent.CompletableFuture;

import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import tomieprofiles.TomieProfiles;
import tomieprofiles.TomieProfiles.CommandResult;

public final class SwapProfileCommand implements SimpleCommand {

    TomieProfiles controller;

    public SwapProfileCommand(TomieProfiles controller){
        this.controller = controller;
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        if(!(source instanceof Player player)){
            return;
        }
        UUID uuid = player.getUniqueId();
        int id;
        try {
            id = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            id = -1;
        }

        if(id == -1) return;

        var result = controller.setNextConnectionMiniProfile(uuid,id);
        if(result==CommandResult.OK){
            final TextComponent textComponent = Component.text().content("You are swapping profiles, try connecting to the server again to use new the profile").build();
            player.disconnect(textComponent);
        }else if(result==CommandResult.OUT_OF_RANGE){
            final TextComponent textComponent = Component.text().content("That profile doesnt exist").color(NamedTextColor.RED).build();
            source.sendMessage(textComponent);
        }else if(result==CommandResult.IN_USE){
            final TextComponent textComponent = Component.text().content("That profile is being used").color(NamedTextColor.RED).build();
            source.sendMessage(textComponent);
        }else{
            final TextComponent textComponent = Component.text().content("Unknown error").color(NamedTextColor.RED).build();
            source.sendMessage(textComponent);
        }
        
    }

    // This method allows you to control who can execute the command.
    // If the executor does not have the required permission,
    // the execution of the command and the control of its autocompletion
    // will be sent directly to the server on which the sender is located
    @Override
    public boolean hasPermission(final Invocation invocation) {
        return true;
        // return invocation.source().hasPermission("command.test");
    }

    // Here you can offer argument suggestions in the same way as the previous method,
    // but asynchronously. It is recommended to use this method instead of the previous one
    // especially in cases where you make a more extensive logic to provide the suggestions
    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }
}
# Introduction

A plugin for Minecraft Velocity servers that allows to connect with the same account to the server several times. The plugin will assign a new username to each new connection using the same account. By using commands the players can change which MiniProfile (sub-profile) they are using. This plugin is heavily inspired by [TwinSession](https://modrinth.com/plugin/chatpatchertwin).

# Requirements

- [configLib](https://github.com/Exlll/ConfigLib)

# Features

When a player with the account of and already connected player tries to connect to the proxy, they'll get a new profile with a different name and info. These profiles are numbered starting from 0. If a player wants to use a diferent profile, they can use the command `profileswap` followed by and argument indicanting the miniprofile id, like this:
`/profileswap 1`, this will kick the player out of the server and when they reconnect, they'll be using the miniprofile 1.

The config file should look like this.

```
servers:
  - serverName: "survival"
    overwriteMessages: false
  - serverName: "creative"
    overwriteMessages: false
```

overwriteMessages is false by default

# Known issues

- Chat messages don't work properly, so in each server, you can change enforce-secure-profile (server.properties) to false. If you dont want to change that option, in the config file of the plugin, you have to set overwriteMessages to true. Some minecraft versions (mainly newer versions) do not allow to overwrite chat messages, so you'll need a plugin like [ChatPatcher](https://modrinth.com/plugin/chatpatcher) to be able to use it. ChatPatcher comes with some other issues, its a proxy level plugin, so it'll affect all the subservers. It'll also have problems if enforce-secure-profile is set to false.
- MiniProfiles can't be created without trying to connect with the same account first.
- Even if the second player connects to a diferent subserver, they'll get the new profile instead of the base one.

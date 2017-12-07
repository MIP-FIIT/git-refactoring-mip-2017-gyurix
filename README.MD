#Refactoring

I have refactored the 4 classes of the whole SpigotLib plugin.
The commits of the refactoring started after commit with message
**Added recent changes, starting refactoring after this commit**. 

###BungeeAPI
- Converted code to use the new Java 8 features, such as lamdas
- Created JavaDOC
- Merged several HashMaps to fewer HashMaps by creating inner classes.
- Moved things having similar functionality to classes 
- Removed useless brackets
- Renamed method parameters and local variables
- Splitted longer methods to the combination of shorter ones

###TabAPI
- Added static imports for better code readability
- Created JavaDOC
- Renamed some method parameters and local variables
- Replaced some loops with new Java 8 forEach and lamda features

###TitleAPI
- Created JavaDOC
- Renamed some method parameters and local variables
- Replaced some loops with new Java 8 forEach and lamda features

###VariableAPI
- Commented code in method bodies
- Created JavaDOC
- Fixed field visibility related issues (converted some public fields to private fields)
- Removed magic numbers (character code ids used instead of characters)
- Renamed some method parameters and local variables
- Replaced some loops with new Java 8 forEach and lamda features



#SpigotLib

##Introduction

SpigotLib is a collection of several useful libraries and APIs
for Spigot plugin development. It's not only perfect for developers,
but it's also good for server owners, because they can use it's API
testing commands, which are really useful.

##Compatibility

###Incompatible server versions:
1.8.0

###Compatible Spigot/PaperSpigot versions:
1.8.8; 1.9; 1.9.2; 1.9.4; 1.10; 1.10.1; 1.10.2; 1.11; 1.11.2; 1.12​; 1.12.2

Every here not listed server version should be either fully compatible
(similar or newer server versions) or minimally compatible (older server versions),
so even if you use the plugin on 1.7 or 1.6 servers,
you can still use some of it's features.

##Features

- **Animation API** (make your animations and run them easily)
- **BungeeAPI** (Communicate super easily with the BungeeCord or with other servers connected to BungeeCord)
- **ChatAPI** (for sending Action bar, Chat and System JSON messages really easy, using my really simple custom format)
- **CommandAPI** (make command simulations easily using this API. It provides commands like Title, Subtitle, ActionBar, Console, Chat, Message, e.t.c. And every command is executed through the VariableAPI, so you can also use variables in the commands. You can even create custom commands too)
- **Config management** (Don't waste your time with config loading / saving. This system simplifiest all your Config management. From now it works with MySQL too.)
- **EconomyAPI** (a simple but really useful EconomyAPI system with multiple balance types, Vault compatibility and bank support. Now it supports working with other economy management plugins too.)
- **ItemGuiAPI** (Make custom item GUIs easily, supports inventory layout management so you can set up the same item to several inventory slots easily)
- **JSON API** (Are you tired of using Gson, it's time to try out a way better JsonAPI system. This system works similiar to the configuration management, just with Jsons)
- **Language management API** (use per player selectable languages for your plugins language file)
- **Map API** (manage custom maps very easily and super fast on packet level)
- **Mojang API** (easily handle the requests and responses between your server and between the Mojangs official API)
- **MySQL API** (Never worry about MySQL connections anymore, just use this API to easily send MySQL commands and get the answers or just do some background/Async updates)
- **NBT API** (get, list and add custom NBT tags for entities and items)
- **TPS meter and crashreporter** (make a crash report from the per thread stack trace dumps, if the servers tps is too low)
- **TitleAPI** (for setting Titles/Subtitles, for players without version incompatibility)
- **PacketAPI** (I have created a brand new API for managing packets really easily and compatible between different Spigot versions)
- **PlayerFile** (A brilliant way of storing any kind of data about a player. It supports both FILE and MySQL backend.)
- **RomanNumsAPI** (A simple but powerful API for converting numbers to roman ones and vice versa)
- **ScoreboardAPI** (manage per player different NO FLICKER sidebars, tabbars and nametagbars easily)
- **VariableAPI** (for handling unlimited amount of variables cross plugins, with a really simple and easy to use way)
- **TimeAPI** (for showing times in chat easily)

##Commands
- **/sl cmd** [p:player] <command>
- **/sl debug** 
- **/sl item [give]** [p:player] [item]
- **/sl lang** [p:player] [language]
- **/sl migratetodb**
- **/sl perm** [p:player] [permission]
- **/sl reload** [p:player] <pf|lf|config>
- **/sl save** [p:player] <pf>
- **/sl vars** [p:player] [message]
- **/sl velocity** [p:player] <x> <y> <z>

##Permissions
- **spigotlib.debug** (for getting debug messages, like error reports in the chat)
- **spigotlib.command.commandname** (for using a SpigotLib command)

##Translations

- **Available:** English, German, Hungarian, Dutch

If you are able to, please translate this plugin to your language! In translation, you should keep the plugins colors, commands and variable names. You should only change the sentences in messages.

##Contact

Before complaining about something doesn't work, please CONTACT ME.

**Skype:** gyurixdev

**Email:** barathgy@gmail.com

##​License

- You are NOT allowed to COPY any part of the plugins source code to your plugins. It's only shared, to help developers using my plugin by giving them an overview about how the plugin works, and for easy access to the documentation

- If you want to share this plugin, please do it with sharing the link to this Spigot page, so I and the SpigotMc could see it's new downloads

- The plugin is provided as is, you are only allowed to edit it for your OWN server network (not for your customers one)

- If you have a suggested edit in the plugin, please send it to me, so I could check it out, and if I agree with it, then your update could be added to the plugin, and you can be added to the plugins contributor list

- I am accepting any kind of help in the development of this plugin, so if you are able to translate it, do some kind of optimization, or add a new feature, please let me know it

- If your public plugin uses this library, then you MUST inform me about it and if it is a premium resource, then add me to the plugins buyer list, so I could get acess to your plugin

- If you are using this plugin for a private plugin, then you need to tell me the server name / IP where your plugin will be used, so in this situation you are NOT required to send me your plugin
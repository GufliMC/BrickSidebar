# BrickSidebar

A Minecraft library for creating sidebars.

## Install

Get the latest [release](https://github.com/GufliMC/BrickSidebar/releases) and place it in your server.

### Dependencies
* [BrickPlaceholders](https://github.com/GufliMC/BrickPlaceholders)
* [BrickNametags](https://github.com/GufliMC/BrickNametags) (spigot only)

## Config

You can change the settings in the `config.json`.

Change the update speed (delay between updates of placeholders) in milliseconds.
```json
{
  "updateSpeed": "250"
}
```

Give a default sidebar to the players on join, uses [minimessage format](https://docs.adventure.kyori.net/minimessage#format).
```json
{
  "defaultSidebar": {
    "title": "<yellow>WELCOME",
    "lines": [
      "",
      "<rainbow>RAINBOWS!</rainbow>",
      ""
    ]
  }
}
```

## Usage

### Gradle
```
repositories {
    maven { url "https://repo.jorisg.com/snapshots" }
}

dependencies {
    // minestom
    compileOnly 'com.guflimc.brick.sidebar:minestom-api:+'
    
    // spigot
    compileOnly 'com.guflimc.brick.sidebar:spigot-api:+'
}
```

### Javadocs

You can find the javadocs for all platforms [here](https://guflimc.github.io/BrickSidebar)


### Examples
The API works with layering, multiple extensions can push a sidebar, the latest one will be shown to the player. 
The top sidebar can later be removed and the player will see the underlying sidebar.

```java
Sidebar sidebar = new Sidebar(Component.text("title"));
sidebar.appendLines(Component.text("multi"), Component.text("line"), Component.text("text!"));

SpigotSidebarAPI.get().push(player, sidebar);
SpigotSidebarAPI.get().pop(player);
SpigotSidebarAPI.get().remove(player, sidebar);
```


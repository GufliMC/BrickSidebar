# BrickSidebar

A sidebar extension for [Minestom](https://github.com/Minestom/Minestom).

## Install

Get the latest jar file from [Github actions](https://github.com/MinestomBrick/BrickWorlds/actions) 
and place it in the extension folder of your minestom server.

Make sure to also install [BrickPlaceholders](https://github.com/MinestomBrick/BrickPlaceholders).

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

## API

```
repositories {
    maven { url "https://repo.jorisg.com/snapshots" }
}

dependencies {
    implementation 'com.gufli.bricksidebar:api:1.0-SNAPSHOT'
}
```

The API works with layering, multiple extensions can push a sidebar, the latest one will be shown to the player. 
The top sidebar can later be removed and the player will see the underlying sidebar.
```java
Sidebar sidebar = new Sidebar(Component.text("title"));
sidebar.appendLines(Component.text("multi"), Component.text("line"), Component.text("text!"));

SidebarAPI.push(player, sidebar);
SidebarAPI.pop(player);
SidebarAPI.remove(player, sidebar);
```

Sidebars wil automatically evaluate placeholders in it's contents every x milliseconds (configurable).

## Credits

* The [Minestom](https://github.com/Minestom/Minestom) project
* [MiniMessage](https://docs.adventure.kyori.net/minimessage#format)

## Contributing

Check our [contributing info](CONTRIBUTING.md)


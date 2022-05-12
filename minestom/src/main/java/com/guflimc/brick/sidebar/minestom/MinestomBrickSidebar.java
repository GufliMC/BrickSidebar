package com.guflimc.brick.sidebar.minestom;

import com.google.gson.Gson;
import com.guflimc.brick.sidebar.api.Sidebar;
import com.guflimc.brick.sidebar.common.BrickSidebarConfig;
import com.guflimc.brick.sidebar.minestom.api.MinestomSidebarAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extensions.Extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MinestomBrickSidebar extends Extension {

    private static final Gson gson = new Gson();

    @Override
    public void initialize() {
        getLogger().info("Enabling " + nameAndVersion() + ".");

        // load config
        BrickSidebarConfig config;
        try (
                InputStream is = getResource("config.json");
                InputStreamReader isr = new InputStreamReader(is)
        ) {
            config = gson.fromJson(isr, BrickSidebarConfig.class);
        } catch (IOException e) {
            getLogger().error("Cannot load configuration.", e);
            return;
        }

        // init sidebar manager
        MinestomSidebarAPI.registerManager(new MinestomBrickSidebarManager(config.updateSpeed));

        // default sidebar
        if ( config.defaultSidebar != null && config.defaultSidebar.title != null && config.defaultSidebar.lines != null ) {
            Sidebar sidebar = new Sidebar(MiniMessage.miniMessage().deserialize(config.defaultSidebar.title));
            for ( String line : config.defaultSidebar.lines ) {
                sidebar.appendLines(MiniMessage.miniMessage().deserialize(line));
            }

            MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e ->
                    MinestomSidebarAPI.get().push(e.getPlayer(), sidebar));
        }

        // clear data of players on quit
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e -> {
            MinestomSidebarAPI.get().removeAll(e.getPlayer());
        });

        getLogger().info("Enabled " + nameAndVersion() + ".");
    }

    @Override
    public void terminate() {
        getLogger().info("Disabled " + nameAndVersion() + ".");
    }

    private String nameAndVersion() {
        return getOrigin().getName() + " v" + getOrigin().getVersion();
    }

}

package org.minestombrick.sidebar.app;

import com.google.gson.Gson;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extensions.Extension;
import org.minestombrick.sidebar.api.Sidebar;
import org.minestombrick.sidebar.api.SidebarAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BrickSidebar extends Extension {

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
        SidebarAPI.registerManager(new BrickSidebarManager(config.updateSpeed));

        // default sidebar
        if ( config.defaultSidebar != null && config.defaultSidebar.title != null && config.defaultSidebar.lines != null ) {
            Sidebar sidebar = new Sidebar(MiniMessage.get().parse(config.defaultSidebar.title));
            for ( String line : config.defaultSidebar.lines ) {
                sidebar.appendLines(MiniMessage.get().parse(line));
            }

            MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, e ->
                    SidebarAPI.get().push(e.getPlayer(), sidebar));
        }

        // clear data of players on quit
        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, e -> {
            SidebarAPI.get().removeAll(e.getPlayer());
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

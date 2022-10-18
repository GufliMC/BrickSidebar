package com.guflimc.brick.sidebar.spigot;

import com.google.gson.Gson;
import com.guflimc.brick.scheduler.spigot.api.SpigotScheduler;
import com.guflimc.brick.sidebar.api.Sidebar;
import com.guflimc.brick.sidebar.common.BrickSidebarConfig;
import com.guflimc.brick.sidebar.spigot.api.SpigotSidebarAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class SpigotBrickSidebar extends JavaPlugin {

    private static final Gson gson = new Gson();

    @Override
    public void onEnable() {
        saveResource("config.json", false);
        BrickSidebarConfig config;
        try (
                InputStream is = new FileInputStream(new File(getDataFolder(), "config.json"));
                InputStreamReader isr = new InputStreamReader(is)
        ) {
            config = gson.fromJson(isr, BrickSidebarConfig.class);
        } catch (IOException e) {
            getLogger().severe("Cannot load configuration.");
            e.printStackTrace();
            return;
        }

        // init sidebar manager
        SpigotScheduler scheduler = new SpigotScheduler(this, getName());
        SpigotSidebarAPI.registerManager(new SpigotBrickSidebarManager(config.updateSpeed, scheduler));

        // default sidebar
        if ( config.defaultSidebar != null && config.defaultSidebar.title != null && config.defaultSidebar.lines != null ) {
            Sidebar sidebar = new Sidebar(MiniMessage.miniMessage().deserialize(config.defaultSidebar.title));
            for ( String line : config.defaultSidebar.lines ) {
                sidebar.appendLines(MiniMessage.miniMessage().deserialize(line));
            }

            getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onJoin(PlayerJoinEvent event) {
                    SpigotSidebarAPI.get().push(event.getPlayer(), sidebar);
                }
            }, this);

            Bukkit.getOnlinePlayers().forEach(p -> SpigotSidebarAPI.get().push(p, sidebar));
        }

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                SpigotSidebarAPI.get().removeAll(event.getPlayer());
            }
        }, this);

        getLogger().info("Enabled " + nameAndVersion() + ".");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled " + nameAndVersion() + ".");
    }

    private String nameAndVersion() {
        return getDescription().getName() + " v" + getDescription().getVersion();
    }

}

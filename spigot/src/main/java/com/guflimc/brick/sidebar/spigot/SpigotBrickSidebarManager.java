package com.guflimc.brick.sidebar.spigot;

import com.guflimc.brick.placeholders.spigot.api.SpigotPlaceholderAPI;
import com.guflimc.brick.scheduler.spigot.api.SpigotScheduler;
import com.guflimc.brick.sidebar.api.Sidebar;
import com.guflimc.brick.sidebar.spigot.api.SpigotSidebarManager;
import com.guflimc.brick.sidebar.spigot.scoreboard.AbstractScoreboard;
import com.guflimc.brick.sidebar.spigot.scoreboard.PacketScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class SpigotBrickSidebarManager implements SpigotSidebarManager {

    private final Map<Player, Deque<SidebarEntry>> sidebars = new ConcurrentHashMap<>();

    private final boolean placeholdersEnabled;

    public SpigotBrickSidebarManager(int updateSpeed, SpigotScheduler scheduler) {
        placeholdersEnabled = Bukkit.getPluginManager().isPluginEnabled("BrickPlaceholders");

        if ( !placeholdersEnabled ) {
            return;
        }
        scheduler.asyncRepeating(this::update, updateSpeed, TimeUnit.MILLISECONDS);
    }

    @Override
    public void push(@NotNull Player player, @NotNull Sidebar template) {
        if ( !sidebars.containsKey(player) ) {
            sidebars.put(player, new ConcurrentLinkedDeque<>());
        }

        AbstractScoreboard scoreboard = new PacketScoreboard(template.title());
        int size = template.lines().size();
        for ( int i = 0; i < size; i++ ) {
            scoreboard.addLine(template.lines().get(i));
        }

        SidebarEntry entry = new SidebarEntry(template, scoreboard);
        sidebars.get(player).push(entry);

        update(player, entry);

        scoreboard.addViewer(player);
    }

    @Override
    public Optional<Sidebar> pop(@NotNull Player player) {
        if ( !sidebars.containsKey(player) ) {
            return Optional.empty();
        }

        SidebarEntry entry = sidebars.get(player).poll();
        if ( entry == null ) {
            return Optional.empty();
        }

        entry.scoreboard.removeViewer(player);

        if ( !sidebars.get(player).isEmpty() ) {
            peekEntry(player).ifPresent(e -> {
                update(player, e);
                e.scoreboard.addViewer(player);
            });
        }

        return Optional.of(entry.template());
    }

    @Override
    public void remove(@NotNull Player player, @NotNull Sidebar sidebar) {
        if ( !sidebars.containsKey(player) ) {
            return;
        }

        Optional<SidebarEntry> entry = peekEntry(player);
        if ( entry.isPresent() && entry.get().template() == sidebar ) {
            pop(player);
            return;
        }

        sidebars.get(player).stream().filter(e -> e.template() == sidebar)
                .forEach(e -> sidebars.get(player).remove());
    }

    @Override
    public Optional<Sidebar> peek(@NotNull Player player) {
        return peekEntry(player).map(SidebarEntry::template);
    }

    @Override
    public void removeAll(@NotNull Player player) {
        if ( !sidebars.containsKey(player) ) {
            return;
        }

        peekEntry(player).ifPresent(entry -> entry.scoreboard.removeViewer(player));
        sidebars.remove(player);
    }

    private Optional<SidebarEntry> peekEntry(Player player) {
        if ( !sidebars.containsKey(player) ) {
            return Optional.empty();
        }
        return Optional.ofNullable(sidebars.get(player).peek());
    }

    private void update(Player player, SidebarEntry entry) {
        if ( !placeholdersEnabled ) {
            return;
        }

        for ( int i = 0; i < entry.template().lines().size(); i++ ) {
            entry.scoreboard.setLine(i, SpigotPlaceholderAPI.get().replace(player, entry.template().lines().get(i)));
        }
    }

    private void update(Player player) {
        peekEntry(player).ifPresent(entry -> update(player, entry));
    }

    private void update() {
        sidebars.keySet().forEach(this::update);
    }

    private record SidebarEntry(Sidebar template, AbstractScoreboard scoreboard) {
    }

}
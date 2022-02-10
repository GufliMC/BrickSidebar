package com.gufli.bricksidebar.app;

import com.gufli.brickplaceholders.api.PlaceholderAPI;
import com.gufli.bricksidebar.api.Sidebar;
import com.gufli.bricksidebar.api.SidebarManager;
import com.gufli.brickutils.scheduling.SchedulerAPI;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class BrickSidebarManager implements SidebarManager {

    private final Map<Player, Deque<SidebarEntry>> sidebars = new ConcurrentHashMap<>();

    private final boolean placeholdersEnabled;

    public BrickSidebarManager(int updateSpeed) {
        placeholdersEnabled = MinecraftServer.getExtensionManager().hasExtension("brickplaceholders");

        if ( !placeholdersEnabled ) {
            return;
        }
        SchedulerAPI.get().asyncRepeating(this::update, updateSpeed, TimeUnit.MILLISECONDS);
    }

    @Override
    public void push(@NotNull Player player, @NotNull Sidebar template) {
        if ( !sidebars.containsKey(player) ) {
            sidebars.put(player, new ConcurrentLinkedDeque<>());
        }

        net.minestom.server.scoreboard.Sidebar sidebar = new net.minestom.server.scoreboard.Sidebar(template.title());
        int size = template.lines().size();
        for ( int i = 0; i < size; i++ ) {
            sidebar.createLine(new net.minestom.server.scoreboard.Sidebar.ScoreboardLine(i + "", template.lines().get(i), size - i));
        }

        SidebarEntry entry = new SidebarEntry(template, sidebar);
        sidebars.get(player).push(entry);

        update(player, entry);

        sidebar.addViewer(player);
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

        entry.sidebar.removeViewer(player);

        if ( !sidebars.get(player).isEmpty() ) {
            peekEntry(player).ifPresent(e -> {
                update(player, e);
                e.sidebar.addViewer(player);
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

        peekEntry(player).ifPresent(entry -> entry.sidebar().removeViewer(player));
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
            entry.sidebar.updateLineContent(i + "", PlaceholderAPI.get().replace(player, entry.template().lines().get(i)));
        }
    }

    private void update(Player player) {
        peekEntry(player).ifPresent(entry -> update(player, entry));
    }

    private void update() {
        sidebars.keySet().forEach(this::update);
    }

    private record SidebarEntry(Sidebar template,
                                net.minestom.server.scoreboard.Sidebar sidebar) {
    }

}
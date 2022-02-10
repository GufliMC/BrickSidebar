package com.gufli.bricksidebar.api;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SidebarManager {

    void push(@NotNull Player player, @NotNull Sidebar sidebar);

    Optional<Sidebar> pop(@NotNull Player player);

    void remove(@NotNull Player player, @NotNull Sidebar sidebar);

    Optional<Sidebar> peek(@NotNull Player player);

    void removeAll(@NotNull Player player);

}
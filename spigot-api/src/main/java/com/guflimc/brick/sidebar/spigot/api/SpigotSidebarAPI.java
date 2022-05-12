package com.guflimc.brick.sidebar.spigot.api;

import org.jetbrains.annotations.ApiStatus;

public class SpigotSidebarAPI {

    private static SpigotSidebarManager sidebarManager;

    @ApiStatus.Internal
    public static void registerManager(SpigotSidebarManager manager) {
        sidebarManager = manager;
    }

    //

    /**
     * Get the registered sidebar manager
     * @return the sidebar manager
     */
    public static SpigotSidebarManager get() {
        return sidebarManager;
    }

}
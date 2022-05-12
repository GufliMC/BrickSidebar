package com.guflimc.brick.sidebar.minestom.api;

import org.jetbrains.annotations.ApiStatus;

public class MinestomSidebarAPI {

    private static MinestomSidebarManager sidebarManager;

    @ApiStatus.Internal
    public static void registerManager(MinestomSidebarManager manager) {
        sidebarManager = manager;
    }

    //

    /**
     * Get the registered sidebar manager
     * @return the sidebar manager
     */
    public static MinestomSidebarManager get() {
        return sidebarManager;
    }

}
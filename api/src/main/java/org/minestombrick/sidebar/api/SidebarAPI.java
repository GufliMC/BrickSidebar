package org.minestombrick.sidebar.api;

public class SidebarAPI {

    private static SidebarManager sidebarManager;

    public static void registerManager(SidebarManager manager) {
        sidebarManager = manager;
    }

    //

    public static SidebarManager get() {
        return sidebarManager;
    }

}
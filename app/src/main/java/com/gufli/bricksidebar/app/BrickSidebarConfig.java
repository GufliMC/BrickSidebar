package com.gufli.bricksidebar.app;

import java.util.List;

public class BrickSidebarConfig {

    public int updateSpeed = 250;
    public SidebarTemplate defaultSidebar;

    public static class SidebarTemplate {

        public String title;
        public List<String> lines;

    }

}

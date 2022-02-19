package org.minestombrick.sidebar.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sidebar {

    private Component title;
    private List<Component> lines;

    public Sidebar(@NotNull Component title) {
        this.title = title;
        this.lines = new ArrayList<>();
    }

    public Sidebar(@NotNull Component title, List<Component> lines) {
        this.title = title;
        this.lines = new ArrayList<>(lines);
    }

    public Component title() {
        return title;
    }

    public List<Component> lines() {
        return lines;
    }

    public Sidebar setTitle(Component title) {
        this.title = title;
        return this;
    }

    public Sidebar setLines(List<Component> lines) {
        this.lines = new ArrayList<>(lines);
        return this;
    }

    public Sidebar appendLines(Component... lines) {
        this.lines.addAll(Arrays.asList(lines));
        return this;
    }

}

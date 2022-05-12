package com.guflimc.brick.sidebar.spigot.scoreboard;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractScoreboard {

    protected final String objectiveId;

    protected final Component title;
    protected final Set<Player> viewers = new HashSet<>();
    protected final List<Component> lines = new ArrayList<>();

    public AbstractScoreboard(Component title) {
        this.objectiveId = RandomStringUtils.randomAlphabetic(15);
        this.title = title;
    }

    public void addViewer(Player player) {
        this.viewers.add(player);
    }

    public void removeViewer(Player player) {
        this.viewers.remove(player);
    }

    public Collection<Player> viewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public void addLine(Component line) {
        this.lines.add(line);
    }

    public void removeLine(int index) {
        this.lines.remove(index);
    }

    public void setLine(int index, Component line) {
        this.lines.set(index, line);
    }

}

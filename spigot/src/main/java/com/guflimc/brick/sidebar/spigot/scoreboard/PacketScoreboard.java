package com.guflimc.brick.sidebar.spigot.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PacketScoreboard extends BaseScoreboard {

    protected static final int SIDEBAR_SLOT = 1;

    private final Map<Component, Integer> scoreCache = new ConcurrentHashMap<>();

    public PacketScoreboard(Component title) {
        super(title);
    }

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
        sendShowPacket(player);
    }

    @Override
    public void removeViewer(Player player) {
        super.removeViewer(player);
        sendHidePacket(player);
    }

    @Override
    public void addLine(Component line) {
        super.addLine(line);
        update();
    }

    @Override
    public void removeLine(int index) {
        super.removeLine(index);
        update();
    }

    @Override
    public void setLine(int index, Component line) {
        super.setLine(index, line);
        update();
    }

    //

    private void update() {
        Set<Component> passed = new HashSet<>();

        int size = lines.size();
        for (int i = 0; i < size; i++) {
            Component line = lines.get(i);
            while (passed.contains(line)) {
                line = line.append(Component.text(" "));
            }

            setScore(line, size - i);
            passed.add(line);
        }

        scoreCache.keySet().stream()
                .filter(key -> !passed.contains(key))
                .forEach(this::removeScore);

    }

    private void setScore(Component text, int value) {
        Integer oldVal = scoreCache.put(text, value);
        if (oldVal != null && oldVal == value) {
            return;
        }

        updateScorePacket(text, value, EnumWrappers.ScoreboardAction.CHANGE);
    }

    private void removeScore(Component text) {
        scoreCache.remove(text);
        updateScorePacket(text, 0, EnumWrappers.ScoreboardAction.REMOVE);
    }


    // PACKETS

    private final static GsonComponentSerializer GSON = GsonComponentSerializer.gson();

    protected void sendShowPacket(Player player) {
        PacketContainer packet = objectivePacket(0);
        packet.getChatComponents().write(0, WrappedChatComponent.fromJson(GSON.serialize(title)));
        sendPacket(player, packet);

        packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        packet.getStrings().write(0, objectiveId);
        packet.getIntegers().write(0, SIDEBAR_SLOT);
        sendPacket(player, packet);

        sendInitialScorePackets(player);
    }

    protected void sendHidePacket(Player player) {
        PacketContainer packet = objectivePacket(1);
        sendPacket(player, packet);
    }

    private PacketContainer objectivePacket(int action) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
        packet.getStrings().write(0, objectiveId);
        packet.getIntegers().write(0, action);
        packet.getEnumModifier(EnumScoreboardHealthDisplay.class, 2).write(0, EnumScoreboardHealthDisplay.INTEGER);
        return packet;
    }

    protected void sendPacket(Player player, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException ignore) {
        }
    }

    protected enum EnumScoreboardHealthDisplay {
        INTEGER,
        HEARTS
    }

    // SCORES

    private final static String UNIQUEID = "BRSB";
    private final Map<Component, PacketTeam> teams = new HashMap<>();
    private int counter = 0;

    private void sendInitialScorePackets(Player player) {
        teams.forEach((c, team) -> {
            team.show(player);
            sendScorePacket(player, team.member(), scoreCache.get(c), EnumWrappers.ScoreboardAction.CHANGE);
        });
    }

    private void sendScorePacket(String text, int score, EnumWrappers.ScoreboardAction action) {
        PacketContainer packet = scorePacket(text, score, action);
        viewers.forEach(player -> sendPacket(player, packet));
    }

    private void sendScorePacket(Player player, String text, int score, EnumWrappers.ScoreboardAction action) {
        PacketContainer packet = scorePacket(text, score, action);
        sendPacket(player, packet);
    }

    private PacketContainer scorePacket(String text, int score, EnumWrappers.ScoreboardAction action) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);
        packet.getStrings().write(0, text);
        packet.getStrings().write(1, objectiveId);
        packet.getIntegers().write(0, score);
        packet.getScoreboardActions().write(0, action);
        return packet;
    }

    //

    private void updateScorePacket(Component text, int score, EnumWrappers.ScoreboardAction action) {
        // remove line
        if (action == EnumWrappers.ScoreboardAction.REMOVE) {
            PacketTeam team = teams.remove(text);
            if (team == null) return;

            sendScorePacket(team.member(), score, action);
            viewers.forEach(team::hide);
            return;
        }

        // get team
        PacketTeam team = teams.get(text);
        if (team != null) {
            // team already exists
            sendScorePacket(team.member(), score, action);
            return;
        }

        // create team
        String id = UNIQUEID + RandomStringUtils.randomAlphanumeric(5) + score;
        team = new PacketTeam(id, text, name(++counter));
        teams.put(text, team);

        viewers.forEach(team::show);
        sendScorePacket(team.member(), score, action);
    }

    private String name(int index) {
        int size = ChatColor.values().length;
        StringBuilder result = new StringBuilder();
        while (index >= 0) {
            result.append(ChatColor.values()[index % size]);
            index -= size;
        }
        return result.toString();
    }

}

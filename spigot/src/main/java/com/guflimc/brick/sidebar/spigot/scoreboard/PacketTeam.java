package com.guflimc.brick.sidebar.spigot.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class PacketTeam {

    private final String id;
    private final Component text;

    private final String member;

    public PacketTeam(String id, Component text, String member) {
        this.id = id;
        this.text = text;
        this.member = member;
    }

    public String id() {
        return id;
    }

    public Component text() {
        return text;
    }

    public String member() {
        return member;
    }

    //

    public void hide(Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(0, 1); // 1 = REMOVED
        packet.getStrings().write(0, id);
        send(player, packet);
    }

    public void show(Player receiver) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(0, 0); // 2 = TEAM_CREATED
        packet.getStrings().write(0, id);
        fillValues(packet);
        send(receiver, packet);

        packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getIntegers().write(0, 3); // 3 = PLAYERS_REMOVED
        packet.getStrings().write(0, id);
        packet.getSpecificModifier(Collection.class).write(0, Collections.singletonList(member));
        send(receiver, packet);
    }

    private void send(Player receiver, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Cannot send packet.", e);
        }
    }

    private final static GsonComponentSerializer GSON = GsonComponentSerializer.gson();

    private void fillValues(PacketContainer packet) {
        try {
            Class<?> scoreboardClass = Class.forName("net.minecraft.world.scores.Scoreboard");
            Class<?> scoreboardTeamClass = Class.forName("net.minecraft.world.scores.ScoreboardTeam");
            Object scoreboardTeam = scoreboardTeamClass.getConstructor(scoreboardClass, String.class).newInstance(null, id);

            StructureModifier<?> m = new StructureModifier<>(scoreboardTeamClass).withTarget(scoreboardTeam);

            StructureModifier<WrappedChatComponent> wccm = m.withType(
                    MinecraftReflection.getIChatBaseComponentClass(), BukkitConverters.getWrappedChatComponentConverter());

            wccm.write(0, WrappedChatComponent.fromLegacyText(id));
            wccm.write(1, WrappedChatComponent.fromJson(GSON.serialize(text)));

            Class<?> dataClass = packet.getType().getPacketClass().getDeclaredClasses()[0];
            Object dataObject = dataClass.getConstructor(scoreboardTeamClass).newInstance(scoreboardTeam);
            packet.getModifier().withType(Optional.class).write(0, Optional.of(dataObject));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

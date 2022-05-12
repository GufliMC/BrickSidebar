package com.guflimc.brick.sidebar.api;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SidebarManager<T> {

    void push(@NotNull T entity, @NotNull Sidebar sidebar);

    Optional<Sidebar> pop(@NotNull T entity);

    void remove(@NotNull T entity, @NotNull Sidebar sidebar);

    Optional<Sidebar> peek(@NotNull T entity);

    void removeAll(@NotNull T entity);

}
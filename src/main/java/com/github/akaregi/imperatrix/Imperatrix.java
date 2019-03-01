/*
 * This file is a part of Imperatrix.
 *
 * Imperatrix, a PlaceholderAPIg expansion.
 * Copyright (C) 2019 akaregi <akg.tachibana@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <https://www.gnu.org/licenses/>.
 */

package com.github.akaregi.imperatrix;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * Imperatrix, a PlaceholderAPI expansion.
 *
 * @author akaregi
 */
public class Imperatrix extends PlaceholderExpansion {
    @Getter(onMethod = @__({@Override}))
    final String author = "akaregi";

    @Getter(onMethod = @__({@Override}))
    final String version = getClass().getPackage().getImplementationVersion();

    @Getter(onMethod = @__({@Override}))
    final String identifier = "Imperatrix";

    private Object server;
    private String serverVer;

    public Imperatrix() {
        try {
            serverVer = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            server = Class.forName("net.minecraft.server." + serverVer + ".MinecraftServer")
                    .getMethod("getServer").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements IMPERATRIX's placeholder, for instance %imperatrix_tps% .
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param player PLAYER.
     * @param identifier Placeholder identifier, like %XXX_identifier%
     *
     * @return TPS value as String if success, or empty String.
     */
    public String onPlaceholderRequest(Player player, String identifier) {
        if (!(identifier.startsWith("tps"))) return "";

        try {
            return String.valueOf(Math.min(round(getTPS(server)[0]), 20.0));
        } catch (NoSuchFieldException | IllegalAccessException  e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Rounds double value, like 20.00 .
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param d value of double.
     *
     * @return Rounded value.
     */
    private static double round(double d) {
        return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Gets TPS (Tick Per Second) using NMS Reflection.
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param server Minecraft Server, from NMS invocation.
     *
     * @return TPS array, [double, double, double].
     */
    private double[] getTPS(Object server) throws IllegalAccessException, NoSuchFieldException {
        return (double[]) server.getClass().getField("recentTps").get(server);
    }
}

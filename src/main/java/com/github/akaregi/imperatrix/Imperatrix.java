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
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;
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

        if (identifier.toLowerCase().startsWith("permprefix_")) return this.getPrefix(player, identifier);

        if (identifier.equalsIgnoreCase("tps")) return this.getTps();

        return "";
    }
    /**
     * Gets TPS trancated if the value is over 20
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param server Minecraft Server, from NMS invocation.
     *
     * @return TPS less than 20.
     */
    public String getTps(){
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
    /**
     * Find permission prefix and add it to input prefix.
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     * @param player PLAYER.
     * @param identifier
     *
     * @return Prefix based on player's permission.
     */
    public String getPrefix(Player player, String identifier){

        String Prefix = identifier.substring(11);
        String PermPrefix = "";

        Map<String, String> pairs = new LinkedHashMap<String, String>(){

			private static final long serialVersionUID = 1L;

			{
                put("select.gradeprefix.mod2", "&d&l#&r");
                put("select.gradeprefix.mod", "&d#&r");
                put("select.gradeprefix.vip", "&e*&r");
                put("select.gradeprefix.citizens", "&b*&r");
                put("select.gradeprefix.default", "&a*&r");
                put("group.admins", "&6#&r");
                put("group.mod2", "&d&l#&r");
                put("group.mod", "&d#&r");
                put("group.vip", "&e*&r");
                put("group.citizens", "&b*&r");
                put("group.default", "&a*&r");
            }
        };

        for(Map.Entry<String, String> perm : pairs.entrySet()){
            if(player.hasPermission(perm.getKey())){
                PermPrefix = perm.getValue();
                break;
            }
        }

        return Prefix + PermPrefix;

    }

}
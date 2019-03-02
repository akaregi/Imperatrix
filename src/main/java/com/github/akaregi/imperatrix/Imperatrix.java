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

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import com.github.akaregi.imperatrix.lib.ServerTPS;
import com.github.akaregi.imperatrix.lib.PlayerPlaceholder;

/**
 * Imperatrix, a PlaceholderAPI expansion.
 *
 * @author akaregi
 */
public class Imperatrix extends PlaceholderExpansion {
    /**
     * この PlaceholderAPI 拡張の作者。
     */
    @Getter(onMethod = @__({@Override}))
    final String author = "akaregi";

    /**
     * この PlaceholderAPI 拡張のバージョン。
     */
    @Getter(onMethod = @__({@Override}))
    final String version = getClass().getPackage().getImplementationVersion();

    /**
     * この PlaceholderAPI 拡張の識別子。
     */
    @Getter(onMethod = @__({@Override}))
    final String identifier = "Imperatrix";

    /**
     * net.minecraft.server インスタンス。
     */
    private Object server;


    /**
     * サーバーインスタンスのバージョン。
     */
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
     * @param player     PLAYER.
     * @param identifier Placeholder identifier, like %XXX_identifier%
     *
     * @return TPS value as String if success, or empty String.
     */
    public String onPlaceholderRequest(Player player, String identifier) {

        if (identifier.toLowerCase().startsWith("hasitem_"))
            return String.valueOf(PlayerPlaceholder.hasItem(player, identifier));

        if (identifier.toLowerCase().startsWith("permprefix_"))
            return this.getPrefix(player, identifier);

        if (identifier.equalsIgnoreCase("tps")) {
            try {
                return String.valueOf(ServerTPS.getRationalTPS(server)[0]);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();

                return "";
            }
        }

        return "";
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

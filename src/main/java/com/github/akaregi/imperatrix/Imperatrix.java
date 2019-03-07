/*
 * This file is a part of Imperatrix.
 *
 * Imperatrix, a PlaceholderAPIg expansion. Copyright (C) 2019 akaregi <akg.tachibana@gmail.com>
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

import com.github.akaregi.imperatrix.lib.PlayerLib;
import com.github.akaregi.imperatrix.lib.ServerLib;

/**
 * Imperatrix, a PlaceholderAPI expansion.
 *
 * @author OKOCRAFT
 */
public class Imperatrix extends PlaceholderExpansion {
    /**
     * この PlaceholderAPI 拡張の作者。{@link PlaceholderExpansion#getAuthor()} の実装。
     *
     * @see PlaceholderExpansion#getAuthor()
     */
    @Getter(onMethod = @__({@Override}))
    final String author = "akaregi";

    /**
     * この PlaceholderAPI 拡張のバージョン。{@link PlaceholderExpansion#getVersion()} の実装。
     *
     * @see PlaceholderExpansion#getVersion()
     */
    @Getter(onMethod = @__({@Override}))
    final String version = getClass().getPackage().getImplementationVersion();

    /**
     * この PlaceholderAPI 拡張の識別子。{@link PlaceholderExpansion#getIdentifier()} の実装。
     *
     * @see PlaceholderExpansion#getIdentifier()
     */
    @Getter(onMethod = @__({@Override}))
    final String identifier = "Imperatrix";

    /**
     * net.minecraft.server インスタンス。
     *
     * @see Imperatrix#serverVer
     */
    private Object server;


    /**
     * サーバーインスタンス({@link Imperatrix#server})のバージョン。
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
     * Imperatrix で実装される PAPI プレースホルダのディスパッチ処理を行う。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param player     プレイヤー。
     * @param identifier PAPI プレースホルダの識別子。
     *
     * @return 与えられたプレースホルダの値。真偽値や数値であっても文字列で返される。
     */
    public String onPlaceholderRequest(Player player, String identifier) {

        if (identifier.toLowerCase().startsWith("hasitem_"))
            return String.valueOf(PlayerLib.hasItem(player, identifier));

        if (identifier.toLowerCase().startsWith("permprefix_"))
            return this.getPrefix(player, identifier);

        if (identifier.equalsIgnoreCase("tps")) {
            try {
                return String.valueOf(ServerLib.getRationalTPS(server)[0]);
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
     * @param player     PLAYER.
     * @param identifier
     *
     * @return Prefix based on player's permission.
     */
    public String getPrefix(Player player, String identifier) {

        String Prefix = identifier.substring(11);
        String PermPrefix = "";

        Map<String, String> pairs = new LinkedHashMap<String, String>() {

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

        for (Map.Entry<String, String> perm : pairs.entrySet()) {
            if (player.hasPermission(perm.getKey())) {
                PermPrefix = perm.getValue();
                break;
            }
        }

        return Prefix + PermPrefix;
    }

}

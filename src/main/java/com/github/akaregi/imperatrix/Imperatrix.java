/*
 * This file is a part of Imperatrix.
 *
 * Imperatrix, a PlaceholderAPI expansion.
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

import com.github.akaregi.imperatrix.lib.PlayerLib;
import com.github.akaregi.imperatrix.lib.ServerLib;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Imperatrix, a PlaceholderAPI expansion.
 *
 * @author OKOCRAFT
 */

public class Imperatrix extends PlaceholderExpansion {





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
     * @param player     プレイヤー。
     * @param identifier PAPI プレースホルダの識別子。
     * @return 与えられたプレースホルダの値。真偽値や数値であっても文字列で返される。
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     */

    public String onPlaceholderRequest(Player player, String identifier) {

        if (identifier.toLowerCase().startsWith("hasitem_lorepartialmatch_")) {
            return String.valueOf(PlayerLib.hasItemLorePartialMatch(player, identifier));
        }

        if (identifier.toLowerCase().startsWith("hasitem_")) {
            return String.valueOf(PlayerLib.hasItem(player, identifier));
        }

        if (identifier.toLowerCase().startsWith("holditem_lorepartialmatch_")) {
            return String.valueOf(PlayerLib.holdItemLorePartialMatch(player, identifier));
        }

        if (identifier.equalsIgnoreCase("okopoint")) {
            return String.valueOf(PlayerLib.getOkopoint(player));
        }

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
     * この PlaceholderAPI 拡張の識別子。{@link PlaceholderExpansion#getIdentifier()} の実装。
     *
     * @see PlaceholderExpansion#getIdentifier()
     */
    @Override
    public String getIdentifier() {
        return "Imperatrix";
    }

    /**
     * この PlaceholderAPI 拡張の作者。{@link PlaceholderExpansion#getAuthor()} の実装。
     *
     * @see PlaceholderExpansion#getAuthor()
     */
    @Override
    public String getAuthor() {
        return "okocraft";
    }

    /**
     * この PlaceholderAPI 拡張のバージョン。{@link PlaceholderExpansion#getVersion()} の実装。
     *
     * @see PlaceholderExpansion#getVersion()
     */
    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}

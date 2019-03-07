/*
 * This file is a part of Imperatrix.
 *
 * Imperatrix, a PlaceholderAPI expansion. Copyright (C) 2019 OKOCRAFT
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

package com.github.akaregi.imperatrix.lib.bridge;

import java.util.Objects;

import lombok.val;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;

public class LuckPermsBridge {
    private LuckPermsApi api;

    public LuckPermsBridge() {
    }

    /**
     * @author akaregi
     * @since 1.1.0-SNAPSHOT
     */
    public static LuckPermsBridge load() {
        RegisteredServiceProvider<LuckPermsApi> provider =
                Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);

        if (Objects.nonNull(provider)) {
            val bridge = new LuckPermsBridge();
            bridge.api = provider.getProvider();

            return bridge;
        }

        return null;
    }

    /**
     * プレイヤーが権限を持っているか判定する。
     *
     * @author akaregi
     * @since 1.1.0-SNAPSHOT
     *
     * @param player 調査先のプレイヤー。
     * @param node   調査する権限ノード。
     *
     * @return 権限を持っているなら {@code true} 、さもなくば {@code false} 。
     */
    public boolean hasPermission(Player player, String node) {
        return api.getUser(player.getName()).getNodes().containsKey(node);
    }

    /**
     * プレイヤーの接頭辞を取得する。
     *
     * @author akaregi
     * @since 1.1.0-SNAPSHOT
     *
     * @param player 接頭辞の取得元プレイヤー。
     *
     * @return 接頭辞。
     */
    public String getPrefix(Player player) {
        return getPlayerMeta(player).getPrefix();
    }

    /**
     * 称号を設定する。
     *
     * <p>
     * {@code newTitle} は「何」など漢字一文字であり [] などはつけない。
     *
     * @author akaregi
     * @since 1.1.0-SNAPSHOT
     *
     * @param player   接頭辞の付与先プレイヤー
     * @param newTitle 新しい称号。
     *
     * @return 付与に成功したら {@code true} 、さもなくば {@code false} 。
     */
    public boolean setTitle(Player player, String newTitle) {
        val meta = getPlayerMeta(player);

        val title = "&7[" + newTitle + "&7]";

        val prefix = meta.getPrefix().replaceAll("&7[.*&7]", title);

        return getUser(player).setPermission(buildPrefixNode(100, prefix)).asBoolean();
    }

    /**
     * LuckPerms 上のユーザを取得する。
     *
     * @author akaregi
     * @since 1.1.0-SNAPSHOT
     *
     * @see User
     * @see LuckPermsApi#getUser(String)
     *
     * @param player 変換元のプレイヤー
     *
     * @return プレイヤーを LuckPerms 上のユーザにしたもの。
     */
    private User getUser(Player player) {
        return api.getUser(player.getName());
    }

    /**
     * プレイヤーのメタデータを取得する。
     *
     * @author akaregi
     * @since 1.1.0-SNAPSHOT
     *
     * @see MetaData
     *
     * @param player メタデータの取得元プレイヤー
     *
     * @return プレイヤーの持つメタデータ。
     */
    private MetaData getPlayerMeta(Player player) {
        val user = getUser(player);
        val contexts = api.getContextManager().getApplicableContexts(player);

        return user.getCachedData().getMetaData(contexts);
    }

    /**
     * 新しく接頭辞の権限ノードを構成する。
     *
     * @author akaregi
     * @since 1.1.0-SNAPSHOT
     *
     * @see Node
     *
     * @param priority LuckPerms 上の優先度。
     * @param prefix   接頭辞。
     *
     * @return 権限ノード。
     */
    private Node buildPrefixNode(int priority, String prefix) {
        return api.getNodeFactory().makePrefixNode(priority, prefix).build();
    }
}
